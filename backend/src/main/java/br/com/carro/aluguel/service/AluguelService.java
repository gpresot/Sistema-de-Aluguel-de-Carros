package br.com.carro.aluguel.service;

import br.com.carro.aluguel.dto.AluguelResponse;
import br.com.carro.aluguel.dto.AluguelResponse.AnaliseFinanceiraResponse;
import br.com.carro.aluguel.model.*;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.AluguelRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fluxo do pedido de aluguel:
 * <pre>
 * PENDENTE --(empresa aceita)--> EM_ANALISE --(banco aprova)--> APROVADO
 *    |                              |
 *    +--(empresa recusa)--> REJEITADO <--(banco reprova)
 * Cliente pode cancelar enquanto PENDENTE, EM_ANALISE ou APROVADO.
 * </pre>
 * A empresa (dona do automóvel) decide se aceita o pedido. Quem faz a análise financeira da renda do
 * cliente é o banco, e só ele aprova ou reprova.
 */
@Service
public class AluguelService {

    /** Percentual máximo da renda mensal que o aluguel pode comprometer. */
    public static final BigDecimal PERCENTUAL_MAXIMO_RENDA = new BigDecimal("0.30");
    private static final long DIAS_POR_MES = 30;

    /** Status que reservam o automóvel. */
    public static final List<StatusAluguel> ATIVOS = List.of(
            StatusAluguel.PENDENTE, StatusAluguel.EM_ANALISE, StatusAluguel.APROVADO);

    private final AluguelRepository alugueis;
    private final AutomovelRepository automoveis;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;

    public AluguelService(AluguelRepository alugueis, AutomovelRepository automoveis,
                          ClienteRepository clientes, AgenteRepository agentes) {
        this.alugueis = alugueis;
        this.automoveis = automoveis;
        this.clientes = clientes;
        this.agentes = agentes;
    }

    // ---------- CLIENTE ----------

    @Transactional
    public AluguelResponse criarPedido(Usuario usuario, Long automovelId, LocalDate dataInicio, LocalDate dataFim) {
        exigirCliente(usuario);
        validarDatas(dataInicio, dataFim);

        Cliente cliente = clientes.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        Automovel automovel = automoveis.findById(automovelId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Automóvel não encontrado."));

        if (automovel.getTipoProprietario() == TipoProprietario.CLIENTE) {
            throw erro(HttpStatus.BAD_REQUEST, "Este automóvel não está disponível para aluguel.");
        }
        if (automovel.getValorDiaria().signum() <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "Este automóvel ainda não possui valor de diária definido.");
        }
        if (alugueis.existsByAutomovelIdAndStatusIn(automovelId, ATIVOS)) {
            throw erro(HttpStatus.CONFLICT, "Este automóvel já possui um aluguel em andamento.");
        }

        Aluguel aluguel = new Aluguel();
        aluguel.setAutomovel(automovel);
        aluguel.setCliente(cliente);
        aluguel.setDataInicio(dataInicio);
        aluguel.setDataFim(dataFim);
        return resposta(alugueis.save(aluguel), usuario);
    }

    @Transactional
    public AluguelResponse cancelarPedido(Usuario usuario, Long aluguelId) {
        exigirCliente(usuario);
        Aluguel aluguel = buscarDoCliente(usuario, aluguelId);
        if (!List.of(StatusAluguel.PENDENTE, StatusAluguel.EM_ANALISE, StatusAluguel.APROVADO)
                .contains(aluguel.getStatus())) {
            throw erro(HttpStatus.CONFLICT, "Só é possível cancelar pedidos pendentes, em análise ou aprovados.");
        }
        aluguel.setStatus(StatusAluguel.CANCELADO);
        return resposta(aluguel, usuario);
    }

    @Transactional
    public AluguelResponse alterarDatas(Usuario usuario, Long aluguelId, LocalDate dataInicio, LocalDate dataFim) {
        validarDatas(dataInicio, dataFim);

        Aluguel aluguel;
        if (usuario.getPerfil() == Perfil.CLIENTE) {
            aluguel = buscarDoCliente(usuario, aluguelId);
            if (aluguel.getStatus() != StatusAluguel.PENDENTE) {
                throw erro(HttpStatus.CONFLICT, "O pedido só pode ser modificado enquanto estiver pendente.");
            }
        } else {
            exigirAgente(usuario);
            aluguel = buscarDoAgente(usuario, aluguelId);
            if (aluguel.getStatus() != StatusAluguel.PENDENTE) {
                throw erro(HttpStatus.CONFLICT, "A empresa só pode modificar pedidos pendentes.");
            }
        }
        aluguel.setDataInicio(dataInicio);
        aluguel.setDataFim(dataFim);
        return resposta(aluguel, usuario);
    }

    @Transactional(readOnly = true)
    public List<AluguelResponse> listarDoCliente(Usuario usuario) {
        exigirCliente(usuario);
        return alugueis.findByClienteUsuarioIdOrderByIdDesc(usuario.getId()).stream()
                .map(a -> resposta(a, usuario)).toList();
    }

    // ---------- EMPRESA (dona do automóvel) ----------

    /** A empresa aceita o pedido e o encaminha para a análise financeira do banco. */
    @Transactional
    public AluguelResponse aceitarPedido(Usuario usuario, Long aluguelId) {
        exigirAgente(usuario);
        Aluguel aluguel = buscarDoAgente(usuario, aluguelId);
        exigirStatus(aluguel, StatusAluguel.PENDENTE, "Só é possível aceitar pedidos pendentes.");
        aluguel.setStatus(StatusAluguel.EM_ANALISE);
        return resposta(aluguel, usuario);
    }

    @Transactional
    public AluguelResponse recusarPedido(Usuario usuario, Long aluguelId, String motivo) {
        exigirAgente(usuario);
        Aluguel aluguel = buscarDoAgente(usuario, aluguelId);
        exigirStatus(aluguel, StatusAluguel.PENDENTE, "Só é possível recusar pedidos pendentes.");
        aluguel.setStatus(StatusAluguel.REJEITADO);
        aluguel.registrarAvaliacao(agenteDe(usuario), motivo);
        return resposta(aluguel, usuario);
    }

    // ---------- BANCO ----------

    /**
     * Parecer financeiro. Aprovar exige análise de renda positiva; reprovar é sempre permitido.
     */
    @Transactional
    public AluguelResponse parecerFinanceiro(Usuario usuario, Long aluguelId, boolean aprovar, String motivo) {
        exigirBanco(usuario);
        Aluguel aluguel = buscarPorId(aluguelId);
        exigirStatus(aluguel, StatusAluguel.EM_ANALISE, "Só é possível avaliar pedidos em análise.");

        if (aprovar) {
            AnaliseFinanceiraResponse analise = analisarFinanceiramente(aluguel);
            if (!analise.aprovada()) {
                throw erro(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Análise financeira negativa: o aluguel comprometeria R$ " + analise.parcelaMensal()
                                + " por mês, acima do limite de R$ " + analise.limiteParcela()
                                + " (30% da renda de R$ " + analise.rendaMensal() + ").");
            }
        } else if (motivo == null || motivo.isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o motivo da reprovação.");
        }

        aluguel.setStatus(aprovar ? StatusAluguel.APROVADO : StatusAluguel.REJEITADO);
        aluguel.registrarAvaliacao(agenteDe(usuario), motivo);
        return resposta(aluguel, usuario);
    }

    /**
     * Compara o valor mensal que o aluguel compromete (diária x dias do período, no máximo 30)
     * com 30% da soma dos rendimentos do cliente.
     */
    @Transactional(readOnly = true)
    public AnaliseFinanceiraResponse analisarFinanceiramente(Aluguel aluguel) {
        BigDecimal renda = aluguel.getCliente().getRendimentos().stream()
                .map(Rendimento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long dias = ChronoUnit.DAYS.between(aluguel.getDataInicio(), aluguel.getDataFim()) + 1;
        BigDecimal parcela = aluguel.getAutomovel().getValorDiaria()
                .multiply(BigDecimal.valueOf(Math.min(dias, DIAS_POR_MES)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal limite = renda.multiply(PERCENTUAL_MAXIMO_RENDA).setScale(2, RoundingMode.HALF_UP);

        boolean aprovada = renda.signum() > 0 && parcela.compareTo(limite) <= 0;
        return new AnaliseFinanceiraResponse(renda.setScale(2, RoundingMode.HALF_UP), parcela, limite, aprovada);
    }

    // ---------- AGENTES (empresa e banco) ----------

    /** Empresa: pedidos dos seus carros. Banco: pedidos em análise, os que avaliou e os de seus carros. */
    @Transactional(readOnly = true)
    public List<AluguelResponse> listarDoAgente(Usuario usuario) {
        exigirAgente(usuario);
        List<Aluguel> lista = usuario.getPerfil() == Perfil.BANCO
                ? alugueis.findParaBanco(usuario.getId(), StatusAluguel.EM_ANALISE)
                : alugueis.findByAutomovelAgenteUsuarioIdOrderByIdDesc(usuario.getId());
        return lista.stream().map(a -> resposta(a, usuario)).toList();
    }

    // ---------- Apoio ----------

    /** O banco vê a análise de renda dos pedidos; os demais perfis não precisam dela. */
    private AluguelResponse resposta(Aluguel a, Usuario quemPede) {
        AnaliseFinanceiraResponse analise = quemPede.getPerfil() == Perfil.BANCO
                ? analisarFinanceiramente(a) : null;
        return AluguelResponse.de(a, analise);
    }

    private Agente agenteDe(Usuario usuario) {
        return agentes.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Agente não encontrado."));
    }

    private void exigirStatus(Aluguel a, StatusAluguel esperado, String mensagem) {
        if (a.getStatus() != esperado) {
            throw erro(HttpStatus.CONFLICT, mensagem);
        }
    }

    private void exigirCliente(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.CLIENTE) {
            throw erro(HttpStatus.FORBIDDEN, "Apenas clientes podem executar esta ação.");
        }
    }

    private void exigirAgente(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.EMPRESA && usuario.getPerfil() != Perfil.BANCO) {
            throw erro(HttpStatus.FORBIDDEN, "Apenas empresas ou bancos podem executar esta ação.");
        }
    }

    private void exigirBanco(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.BANCO) {
            throw erro(HttpStatus.FORBIDDEN, "Somente o banco pode fazer a análise financeira.");
        }
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe as datas do aluguel.");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw erro(HttpStatus.BAD_REQUEST, "A data de início não pode ser no passado.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw erro(HttpStatus.BAD_REQUEST, "A data final não pode ser antes da data inicial.");
        }
    }

    private Aluguel buscarDoCliente(Usuario usuario, Long aluguelId) {
        Aluguel aluguel = buscarPorId(aluguelId);
        if (!aluguel.getCliente().getUsuario().getId().equals(usuario.getId())) {
            throw erro(HttpStatus.FORBIDDEN, "Este pedido não pertence a você.");
        }
        return aluguel;
    }

    private Aluguel buscarDoAgente(Usuario usuario, Long aluguelId) {
        Aluguel aluguel = buscarPorId(aluguelId);
        Agente dono = aluguel.getAutomovel().getAgente();
        if (dono == null || !dono.getUsuario().getId().equals(usuario.getId())) {
            throw erro(HttpStatus.FORBIDDEN, "Este pedido não envolve um automóvel seu.");
        }
        return aluguel;
    }

    private Aluguel buscarPorId(Long aluguelId) {
        return alugueis.findById(aluguelId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Pedido de aluguel não encontrado."));
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
