package br.com.carro.aluguel.config;

import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria as contas de agentes (empresa e banco) na inicialização. Agentes não se cadastram pela API. */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UsuarioRepository usuarios;
    private final AgenteRepository agentes;
    private final PasswordEncoder encoder;
    private final String senha;

    public DataLoader(UsuarioRepository usuarios, AgenteRepository agentes, PasswordEncoder encoder,
                      @Value("${app.seed.agente-senha:}") String senha) {
        this.usuarios = usuarios;
        this.agentes = agentes;
        this.encoder = encoder;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (senha.isBlank()) {
            log.warn("SEED_AGENT_PASSWORD não definida: contas de agentes não foram criadas");
            return;
        }
        criar("empresa@aluguel.com", Perfil.EMPRESA, "Locadora Exemplo", "11111111000111");
        criar("banco@aluguel.com", Perfil.BANCO, "Banco Exemplo", "22222222000122");
    }

    private void criar(String email, Perfil perfil, String nome, String cnpj) {
        if (usuarios.existsByEmail(email)) {
            return;
        }
        Usuario u = new Usuario(email, encoder.encode(senha), perfil);
        agentes.save(new Agente(nome, cnpj, u));
        log.info("Agente criado: {} ({})", email, perfil);
    }
}
