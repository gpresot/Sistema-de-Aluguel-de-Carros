package br.com.carro.aluguel.service;

import br.com.carro.aluguel.exception.NotFoundException;
import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Cliente;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

/** Resolve o usuário autenticado (e-mail do token JWT) para Cliente ou Agente. */
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;

    public UsuarioLogadoService(UsuarioRepository usuarios, ClienteRepository clientes, AgenteRepository agentes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.agentes = agentes;
    }

    public Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    public Cliente cliente(String email) {
        return clientes.findByUsuarioId(usuario(email).getId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }

    public Agente agente(String email) {
        return agentes.findByUsuarioId(usuario(email).getId())
                .orElseThrow(() -> new NotFoundException("Agente não encontrado."));
    }
}
