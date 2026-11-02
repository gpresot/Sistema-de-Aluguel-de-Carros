package br.com.carro.aluguel.config;

import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DadosDemo implements ApplicationRunner {

    private static final String[][] MODELOS = {
            { "Volkswagen", "Fusca" }, { "Volkswagen", "Brasília" },
            { "Chevrolet", "Opala" }, { "Chevrolet", "Chevette" },
            { "Ford", "Galaxie" }, { "Ford", "Corcel" },
            { "Dodge", "Dart" }, { "Willys", "Itamaraty" },
            { "Simca", "Chambord" }, { "Puma", "GTE" }
    };

    private final UsuarioRepository usuarios;
    private final AgenteRepository agentes;
    private final AutomovelRepository automoveis;
    private final PasswordEncoder passwordEncoder;

    public DadosDemo(UsuarioRepository usuarios,
            AgenteRepository agentes,
            AutomovelRepository automoveis,
            PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.agentes = agentes;
        this.automoveis = automoveis;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Agente empresa = obterOuCriarAgente("empresa@teste.com", Perfil.EMPRESA,
                "Locadora Demo", "12345678000199");
        if (automoveis.count() == 0) {
            cadastrarCarros(empresa);
        }
        obterOuCriarAgente("banco@teste.com", Perfil.BANCO,
                "Banco Demo", "98765432000155");
    }

    // Reaproveita o agente se o usuário já existir; senão cria usuário e agente.
    private Agente obterOuCriarAgente(String email, Perfil perfil, String nome, String cnpj) {
        return usuarios.findByEmail(email)
                .map(u -> agentes.findByUsuarioId(u.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Usuário " + email + " sem agente associado")))
                .orElseGet(() -> {
                    Usuario usuario = new Usuario(email,
                            passwordEncoder.encode("123456"), perfil);
                    return agentes.save(new Agente(nome, cnpj, usuario));
                });
    }

    private void cadastrarCarros(Agente empresa) {
        for (int i = 1; i <= 100; i++) {
            String[] m = MODELOS[i % MODELOS.length];
            int ano = 1950 + (i * 7) % 41;
            Automovel automovel = new Automovel(
                    "MAT-%04d".formatted(i),
                    ano,
                    m[0],
                    m[1],
                    "ANT%04d".formatted(i),
                    empresa);
            automovel.definirValorDiaria(java.math.BigDecimal.valueOf(120 + (i % 10) * 15));
            automoveis.save(automovel);
        }
    }
}