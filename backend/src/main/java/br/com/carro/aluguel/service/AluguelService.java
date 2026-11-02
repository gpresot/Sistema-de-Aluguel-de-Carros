package br.com.carro.aluguel.service;

import br.com.carro.aluguel.model.*;
import br.com.carro.aluguel.repository.AluguelRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class AluguelService {

    /** Percentual máximo da renda mensal que a parcela pode comprometer. */
    public static final BigDecimal PERCENTUAL_MAXIMO_RENDA = new BigDecimal("0.30");
    private static final BigDecimal DIAS_POR_MES = new BigDecimal("30");

    private static final List<StatusAluguel> ATIVOS = List.of(
            StatusAluguel.PENDENTE, StatusAluguel.EM_ANALISE, StatusAluguel.APROVADO);

    public record AnaliseFinanceira(BigDecimal rendaMensal, BigDecimal parcelaMensal,
            BigDecimal limiteParcela, boolean aprovada) {
    }

    private final AluguelRepository alugueis;
    private final AutomovelRepository automoveis;
    private final ClienteRepository clientes;

    public AluguelService(AluguelRepository alugueis,
            AutomovelRepository automoveis,
            ClienteRepository clientes) {
        this.alugueis = alugueis;
        this.automoveis = automoveis;
        this.clientes = clientes;
    }

    // ---------- CLIENTE ----------

    @Transactional
    public Aluguel criarPedido(Usuario usuario, Long automovelId, LocalDate dataInicio, LocalDate dataFim) {
        exigirCliente(usuario);
        validarDatas(dataInicio, dataFim);

        Cliente cliente = clientes.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        Automovel automovel = automoveis.findById(automovelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Automóvel não encontrado."));

        if (automovel.getTipoProprietario() == TipoProprietario.CLIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este automóvel não está disponível para aluguel.");
        }

        if (alugueis.existsByAutomovelIdAndStatusIn(automovelId, ATIVOS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este automóvel já possui um aluguel em andamento.");
        }

        Aluguel aluguel = new Aluguel();
        aluguel.setAutomovel(automovel);
        aluguel.setCliente(cliente);
        aluguel.setDataInicio(dataInicio);
        aluguel.setDataFim(dataFim);
        return alugueis.save(aluguel);
    }

    @Transactional
    public Aluguel cancelarPedido(Usuario usuario, Long aluguelId) {
        exigirCliente(usuario);
        Aluguel aluguel = buscarDoCliente(usuario, aluguelId);
        if (aluguel.getStatus() != StatusAluguel.PENDENTE
                && aluguel.getStatus() != StatusAluguel.EM_ANALISE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só é possível cancelar pedidos pendentes ou em análise.");
        }
        aluguel.setStatus(StatusAluguel.CANCELADO);
        return aluguel;
    }

    @Transactional
    public Aluguel alterarDatas(Usuario usuario, Long aluguelId, LocalDate dataInicio, LocalDate dataFim) {
        validarDatas(dataInicio, dataFim);

        Aluguel aluguel;
        if (usuario.getPerfil() == Perfil.CLIENTE) {
            aluguel = buscarDoCliente(usuario, aluguelId);
            if (aluguel.getStatus() != StatusAluguel.PENDENTE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "O pedido só pode ser modificado enquanto estiver pendente.");
            }
        } else {
            exigirAgente(usuario);
            aluguel = buscarDoAgente(usuario, aluguelId);
            if (aluguel.getStatus() != StatusAluguel.PENDENTE
                    && aluguel.getStatus() != StatusAluguel.EM_ANALISE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Só é possível modificar pedidos pendentes ou em análise.");
            }
        }

        aluguel.setDataInicio(dataInicio);
        aluguel.setDataFim(dataFim);
        return aluguel;
    }

    @Transactional(readOnly = true)
    public List<Aluguel> listarDoCliente(Usuario usuario) {
        exigirCliente(usuario);
        return alugueis.findByClienteUsuarioId(usuario.getId());
    }

    // ---------- AGENTE (EMPRESA ou BANCO) ----------

    @Transactional
    public Aluguel iniciarAnalise(Usuario usuario, Long aluguelId) {
        exigirAgente(usuario);
        Aluguel aluguel = buscarDoAgente(usuario, aluguelId);
        if (aluguel.getStatus() != StatusAluguel.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só é possível iniciar análise de pedidos pendentes.");
        }
        aluguel.setStatus(StatusAluguel.EM_ANALISE);
        return aluguel;
    }

    /**
     * Parecer do agente. Aprovar exige análise financeira positiva.
     * Rejeitar não exige análise.
     */
    @Transactional
    public Aluguel darParecer(Usuario usuario, Long aluguelId, boolean aprovar) {
        exigirAgente(usuario);
        Aluguel aluguel = buscarDoAgente(usuario, aluguelId);

        if (aluguel.getStatus() != StatusAluguel.PENDENTE
                && aluguel.getStatus() != StatusAluguel.EM_ANALISE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este pedido já foi avaliado.");
        }

        if (!aprovar) {
            aluguel.setStatus(StatusAluguel.REJEITADO);
            return aluguel;
        }

        AnaliseFinanceira analise = analisarFinanceiramente(aluguel);
        if (!analise.aprovada()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Análise financeira negativa: parcela de R$ " + analise.parcelaMensal()
                            + " acima do limite de R$ " + analise.limiteParcela() + ".");
        }

        aluguel.setStatus(StatusAluguel.APROVADO);
        return aluguel;
    }

    /**
     * Análise financeira: parcela mensal estimada vs. renda somada dos rendimentos.
     */
    @Transactional(readOnly = true)
    public AnaliseFinanceira analisarFinanceiramente(Aluguel aluguel) {
        BigDecimal renda = aluguel.getCliente().getRendimentos().stream()
                .map(Rendimento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal parcela = aluguel.getAutomovel().getValorDiaria()
                .multiply(DIAS_POR_MES)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal limite = renda.multiply(PERCENTUAL_MAXIMO_RENDA)
                .setScale(2, RoundingMode.HALF_UP);

        boolean aprovada = renda.signum() > 0 && parcela.compareTo(limite) <= 0;
        return new AnaliseFinanceira(renda, parcela, limite, aprovada);
    }

    @Transactional(readOnly = true)
    public List<Aluguel> listarDoAgente(Usuario usuario) {
        exigirAgente(usuario);
        return alugueis.findByAutomovelAgenteUsuarioId(usuario.getId());
    }

    private void exigirCliente(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.CLIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas clientes podem executar esta ação.");
        }
    }

    private void exigirAgente(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.EMPRESA && usuario.getPerfil() != Perfil.BANCO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas empresas ou bancos podem executar esta ação.");
        }
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe as datas do aluguel.");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de início não pode ser no passado.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data final não pode ser antes da data inicial.");
        }
    }

    private Aluguel buscarDoCliente(Usuario usuario, Long aluguelId) {
        Aluguel aluguel = buscarPorId(aluguelId);
        if (!aluguel.getCliente().getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este pedido não pertence a você.");
        }
        return aluguel;
    }

    private Aluguel buscarDoAgente(Usuario usuario, Long aluguelId) {
        Aluguel aluguel = buscarPorId(aluguelId);
        Agente dono = aluguel.getAutomovel().getAgente();
        if (dono == null || !dono.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este pedido não envolve um automóvel seu.");
        }
        return aluguel;
    }

    private Aluguel buscarPorId(Long aluguelId) {
        return alugueis.findById(aluguelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido de aluguel não encontrado."));
    }
}