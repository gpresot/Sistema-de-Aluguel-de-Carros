package br.com.carro.aluguel.service;

import br.com.carro.aluguel.dto.AutomovelRequest;
import br.com.carro.aluguel.dto.AutomovelResponse;
import br.com.carro.aluguel.exception.BadRequestException;
import br.com.carro.aluguel.exception.ConflictException;
import br.com.carro.aluguel.exception.ForbiddenException;
import br.com.carro.aluguel.exception.NotFoundException;
import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.FotoAutomovel;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.StatusAluguel;
import br.com.carro.aluguel.model.TipoProprietario;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AluguelRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.FotoAutomovelRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AutomovelService {

    private static final long TAMANHO_MAXIMO_FOTO = 3L * 1024 * 1024;

    /** Foto já lida do banco, pronta para ser enviada na resposta HTTP. */
    public record Foto(String contentType, byte[] conteudo) {
    }

    private final AutomovelRepository automoveis;
    private final AluguelRepository alugueis;
    private final FotoAutomovelRepository fotos;
    private final UsuarioLogadoService logado;

    public AutomovelService(AutomovelRepository automoveis, AluguelRepository alugueis,
                            FotoAutomovelRepository fotos, UsuarioLogadoService logado) {
        this.automoveis = automoveis;
        this.alugueis = alugueis;
        this.fotos = fotos;
        this.logado = logado;
    }

    @Transactional
    public AutomovelResponse cadastrar(String email, AutomovelRequest r) {
        validarUnicidade(r.matricula(), r.placa(), null);
        Usuario usuario = logado.usuario(email);
        Automovel novo = usuario.getPerfil() == Perfil.CLIENTE
                ? new Automovel(r.matricula(), r.ano(), r.marca(), r.modelo(), r.placa(), logado.cliente(email))
                : new Automovel(r.matricula(), r.ano(), r.marca(), r.modelo(), r.placa(), logado.agente(email));
        novo.definirValorDiaria(r.valorDiaria());
        return AutomovelResponse.de(automoveis.save(novo));
    }

    /** Automóveis cadastrados pelo próprio usuário. */
    @Transactional(readOnly = true)
    public List<AutomovelResponse> listarMeus(String email) {
        Usuario usuario = logado.usuario(email);
        List<Automovel> lista = usuario.getPerfil() == Perfil.CLIENTE
                ? automoveis.findByClienteId(logado.cliente(email).getId())
                : automoveis.findByAgenteId(logado.agente(email).getId());
        return lista.stream().map(AutomovelResponse::de).toList();
    }

    /** Catálogo do cliente: só automóveis de agentes (os de clientes não são alugáveis) e sem aluguel ativo. */
    @Transactional(readOnly = true)
    public List<AutomovelResponse> listarDisponiveis(String email) {
        if (logado.usuario(email).getPerfil() != Perfil.CLIENTE) {
            throw new ForbiddenException("Apenas clientes podem ver o catálogo.");
        }
        return automoveis.findDisponiveis(AluguelService.ATIVOS).stream()
                .filter(a -> a.getTipoProprietario() != TipoProprietario.CLIENTE)
                .map(AutomovelResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AutomovelResponse buscar(String email, Long id) {
        return AutomovelResponse.de(doProprietario(email, id));
    }

    @Transactional
    public AutomovelResponse atualizar(String email, Long id, AutomovelRequest r) {
        Automovel automovel = doProprietario(email, id);
        validarUnicidade(r.matricula(), r.placa(), id);
        automovel.atualizarDados(r.matricula(), r.ano(), r.marca(), r.modelo(), r.placa(), r.valorDiaria());
        return AutomovelResponse.de(automovel);
    }

    @Transactional
    public void excluir(String email, Long id) {
        Automovel automovel = doProprietario(email, id);
        if (alugueis.existsByAutomovelId(id)) {
            throw new ConflictException("Este automóvel possui pedidos de aluguel e não pode ser excluído.");
        }
        fotos.deleteById(id);
        automoveis.delete(automovel);
    }

    // ---------- Fotos ----------

    @Transactional
    public AutomovelResponse salvarFoto(String email, Long id, MultipartFile arquivo) {
        Automovel automovel = doProprietario(email, id);
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BadRequestException("Selecione uma imagem.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_FOTO) {
            throw new BadRequestException("A foto deve ter no máximo 3 MB.");
        }
        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Não foi possível ler a imagem enviada.");
        }
        // O tipo é deduzido do conteúdo, não do cabeçalho enviado pelo cliente.
        String tipo = detectarTipo(bytes);
        if (tipo == null) {
            throw new BadRequestException("Formato inválido. Envie uma imagem JPEG, PNG ou WebP.");
        }
        fotos.findById(id).ifPresentOrElse(
                f -> f.substituir(tipo, bytes),
                () -> fotos.save(new FotoAutomovel(id, tipo, bytes)));
        automovel.registrarFoto();
        return AutomovelResponse.de(automovel);
    }

    @Transactional
    public AutomovelResponse removerFoto(String email, Long id) {
        Automovel automovel = doProprietario(email, id);
        fotos.deleteById(id);
        automovel.removerFoto();
        return AutomovelResponse.de(automovel);
    }

    /** Leitura pública (a tag img do navegador não envia o token). */
    @Transactional(readOnly = true)
    public Foto obterFoto(Long id) {
        FotoAutomovel f = fotos.findById(id).orElseThrow(() -> new NotFoundException("Foto não encontrada."));
        return new Foto(f.getContentType(), f.getConteudo());
    }

    private static String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    // ---------- Apoio ----------

    private Automovel doProprietario(String email, Long id) {
        Automovel automovel = automoveis.findById(id)
                .orElseThrow(() -> new NotFoundException("Automóvel não encontrado."));
        Usuario usuario = logado.usuario(email);
        boolean dono = usuario.getPerfil() == Perfil.CLIENTE
                ? automovel.getCliente() != null
                    && automovel.getCliente().getUsuario().getId().equals(usuario.getId())
                : automovel.getAgente() != null
                    && automovel.getAgente().getUsuario().getId().equals(usuario.getId());
        if (!dono) {
            throw new ForbiddenException("Você não tem permissão para acessar este automóvel.");
        }
        return automovel;
    }

    private void validarUnicidade(String matricula, String placa, Long idAtual) {
        automoveis.findByMatricula(matricula.trim()).filter(a -> !a.getId().equals(idAtual))
                .ifPresent(a -> { throw new ConflictException("Já existe um automóvel com essa matrícula."); });
        automoveis.findByPlaca(placa.trim().toUpperCase()).filter(a -> !a.getId().equals(idAtual))
                .ifPresent(a -> { throw new ConflictException("Já existe um automóvel com essa placa."); });
    }
}
