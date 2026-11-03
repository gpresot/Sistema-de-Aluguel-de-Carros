package br.com.carro.aluguel.config;

import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.FotoAutomovel;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.FotoAutomovelRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dados de demonstração: frota de clássicos (um automóvel de cada modelo em {@code MODELOS}), conta de empresa e de banco.
 * As fotos reais são lidas de {@code src/main/resources/fotos-demo/<marca>-<modelo>.jpg}
 * (ex.: {@code volkswagen-fusca.jpg}) e anexadas a todo automóvel que ainda não tenha foto.
 */
@Component
public class DadosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemo.class);

    private static final String PASTA_FOTOS = "fotos-demo/";
    private static final String[] EXTENSOES = { "jpg", "jpeg", "png", "webp" };
    private static final long TAMANHO_MAXIMO_FOTO = 3L * 1024 * 1024;

    /** Quantos automóveis de cada modelo entram na frota de demonstração. */
    private static final int UNIDADES_POR_MODELO = 1;

    /** { marca, modelo, primeiro ano de fabricação no Brasil, último ano }. */
    private static final String[][] MODELOS = {
            { "Volkswagen", "Fusca", "1959", "1986" }, { "Volkswagen", "Brasília", "1973", "1982" },
            { "Chevrolet", "Opala", "1969", "1992" }, { "Chevrolet", "Chevette", "1973", "1993" },
            { "Ford", "Galaxie", "1967", "1983" }, { "Ford", "Corcel", "1968", "1986" },
            { "Dodge", "Dart", "1969", "1981" }, { "Willys", "Itamaraty", "1966", "1971" },
            { "Simca", "Chambord", "1959", "1967" }, { "Puma", "GTE", "1970", "1975" }
    };

    private final UsuarioRepository usuarios;
    private final AgenteRepository agentes;
    private final AutomovelRepository automoveis;
    private final FotoAutomovelRepository fotos;
    private final PasswordEncoder passwordEncoder;
    private final String senhaAgentes;

    public DadosDemo(UsuarioRepository usuarios,
            AgenteRepository agentes,
            AutomovelRepository automoveis,
            FotoAutomovelRepository fotos,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.agente-senha:123456}") String senhaAgentes) {
        this.usuarios = usuarios;
        this.agentes = agentes;
        this.automoveis = automoveis;
        this.fotos = fotos;
        this.passwordEncoder = passwordEncoder;
        this.senhaAgentes = senhaAgentes;
    }

    @Override
    public void run(ApplicationArguments args) {
        Agente empresa = obterOuCriarAgente("empresa@teste.com", Perfil.EMPRESA,
                "Presot Clássicos", "12345678000199");
        if (automoveis.count() == 0) {
            cadastrarCarros(empresa);
        }
        obterOuCriarAgente("banco@teste.com", Perfil.BANCO,
                "Banco Demo", "98765432000155");
        anexarFotos();
    }

    // Reaproveita o agente se o usuário já existir; senão cria usuário e agente.
    private Agente obterOuCriarAgente(String email, Perfil perfil, String nome, String cnpj) {
        return usuarios.findByEmail(email)
                .map(u -> agentes.findByUsuarioId(u.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Usuário " + email + " sem agente associado")))
                .orElseGet(() -> {
                    Usuario usuario = new Usuario(email,
                            passwordEncoder.encode(senhaAgentes), perfil);
                    return agentes.save(new Agente(nome, cnpj, usuario));
                });
    }

    private void cadastrarCarros(Agente empresa) {
        int numero = 1;
        for (int m = 0; m < MODELOS.length; m++) {
            String[] modelo = MODELOS[m];
            int anoMin = Integer.parseInt(modelo[2]);
            int anoMax = Integer.parseInt(modelo[3]);
            for (int u = 0; u < UNIDADES_POR_MODELO; u++) {
                // Espalha as unidades do mesmo modelo ao longo dos anos de fabricação.
                int ano = anoMin + (anoMax - anoMin) * (2 * u + 1) / (2 * UNIDADES_POR_MODELO);
                Automovel automovel = new Automovel(
                        "MAT-%04d".formatted(numero),
                        ano,
                        modelo[0],
                        modelo[1],
                        "ANT%04d".formatted(numero),
                        empresa);
                automovel.definirValorDiaria(BigDecimal.valueOf(120 + m * 15 + u * 20));
                automoveis.save(automovel);
                numero++;
            }
        }
    }

    // ---------- Fotos ----------

    /** Anexa a foto do modelo a cada automóvel sem foto. Idempotente: pode rodar a cada inicialização. */
    private void anexarFotos() {
        Map<String, Optional<Foto>> cache = new HashMap<>();
        int anexadas = 0;
        for (Automovel a : automoveis.findAll()) {
            if (a.getFotoVersao() != null) {
                continue;
            }
            String slug = slug(a.getMarca() + " " + a.getModelo());
            Optional<Foto> foto = cache.computeIfAbsent(slug, this::lerFoto);
            if (foto.isEmpty()) {
                continue;
            }
            fotos.save(new FotoAutomovel(a.getId(), foto.get().contentType(), foto.get().bytes()));
            a.registrarFoto();
            automoveis.save(a);
            anexadas++;
        }
        if (anexadas > 0) {
            log.info("Fotos de demonstração anexadas a {} automóveis", anexadas);
        }
        cache.forEach((slug, foto) -> {
            if (foto.isEmpty()) {
                log.warn("Sem foto para '{}': adicione resources/{}{}.jpg", slug, PASTA_FOTOS, slug);
            }
        });
    }

    private record Foto(String contentType, byte[] bytes) {
    }

    private Optional<Foto> lerFoto(String slug) {
        for (String ext : EXTENSOES) {
            ClassPathResource recurso = new ClassPathResource(PASTA_FOTOS + slug + "." + ext);
            if (!recurso.exists()) {
                continue;
            }
            try (InputStream in = recurso.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                if (bytes.length > TAMANHO_MAXIMO_FOTO) {
                    log.warn("Foto {} ignorada: maior que 3 MB", recurso.getPath());
                    return Optional.empty();
                }
                String tipo = switch (ext) {
                    case "png" -> "image/png";
                    case "webp" -> "image/webp";
                    default -> "image/jpeg";
                };
                return Optional.of(new Foto(tipo, bytes));
            } catch (IOException e) {
                log.warn("Não foi possível ler {}: {}", recurso.getPath(), e.getMessage());
            }
        }
        return Optional.empty();
    }

    /** "Volkswagen Brasília" -> "volkswagen-brasilia". */
    static String slug(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
