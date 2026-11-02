package br.com.carro.aluguel.service;

import br.com.carro.aluguel.dto.AuthResponse;
import br.com.carro.aluguel.dto.LoginRequest;
import br.com.carro.aluguel.dto.RegistroClienteRequest;
import br.com.carro.aluguel.dto.RendimentoDto;
import br.com.carro.aluguel.exception.ConflictException;
import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Cliente;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.Rendimento;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import br.com.carro.aluguel.security.JwtService;
import br.com.carro.aluguel.util.Entradas;
import br.com.carro.aluguel.util.SenhaPolicy;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, ClienteRepository clientes, AgenteRepository agentes,
                       PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.agentes = agentes;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse registrarCliente(RegistroClienteRequest req) {
        String email = Entradas.normalizarEmail(req.email());
        SenhaPolicy.validar(req.senha(), req.confirmacaoSenha());
        if (usuarios.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta cadastrada com este email.");
        }
        if (clientes.existsByCpf(req.cpf())) {
            throw new ConflictException("CPF já cadastrado");
        }
        if (clientes.existsByRg(req.rg())) {
            throw new ConflictException("RG já cadastrado");
        }
        Usuario usuario = new Usuario(email, encoder.encode(req.senha()), Perfil.CLIENTE);
        List<Rendimento> rendimentos = req.rendimentos() == null ? List.of()
                : req.rendimentos().stream().map(this::paraRendimento).toList();
        Cliente cliente = clientes.save(new Cliente(req.nome(), req.rg(), req.cpf(), req.endereco(),
                req.profissao(), usuario, rendimentos));
        return new AuthResponse(jwt.gerar(email, Perfil.CLIENTE), email, Perfil.CLIENTE, cliente.getNome());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmail(Entradas.normalizarEmail(req.email()))
                .orElseThrow(() -> new BadCredentialsException("credenciais inválidas"));
        if (!encoder.matches(req.senha(), u.getSenha())) {
            throw new BadCredentialsException("credenciais inválidas");
        }
        String nome = u.getPerfil() == Perfil.CLIENTE
                ? clientes.findByUsuarioId(u.getId()).map(Cliente::getNome).orElse(u.getEmail())
                : agentes.findByUsuarioId(u.getId()).map(Agente::getNome).orElse(u.getEmail());
        return new AuthResponse(jwt.gerar(u.getEmail(), u.getPerfil()), u.getEmail(), u.getPerfil(), nome);
    }

    private Rendimento paraRendimento(RendimentoDto dto) {
        return new Rendimento(dto.entidadeEmpregadora(), dto.valor());
    }
}
