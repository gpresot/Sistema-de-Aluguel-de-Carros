package br.com.carro.aluguel.service;

import br.com.carro.aluguel.dto.ClienteRequest;
import br.com.carro.aluguel.dto.ClienteResponse;
import br.com.carro.aluguel.exception.ConflictException;
import br.com.carro.aluguel.model.Cliente;
import br.com.carro.aluguel.model.Rendimento;
import br.com.carro.aluguel.repository.AluguelRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import br.com.carro.aluguel.repository.PasswordResetTokenRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD do cadastro do cliente autenticado (o "C" é feito em AuthService.registrarCliente). */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final AluguelRepository alugueis;
    private final AutomovelRepository automoveis;
    private final PasswordResetTokenRepository tokens;
    private final UsuarioLogadoService logado;

    public ClienteService(ClienteRepository clientes, AluguelRepository alugueis, AutomovelRepository automoveis,
                          PasswordResetTokenRepository tokens, UsuarioLogadoService logado) {
        this.clientes = clientes;
        this.alugueis = alugueis;
        this.automoveis = automoveis;
        this.tokens = tokens;
        this.logado = logado;
    }

    @Transactional(readOnly = true)
    public ClienteResponse consultar(String email) {
        return ClienteResponse.de(logado.cliente(email));
    }

    @Transactional
    public ClienteResponse atualizar(String email, ClienteRequest r) {
        Cliente cliente = logado.cliente(email);
        clientes.findByCpf(r.cpf()).filter(c -> !c.getId().equals(cliente.getId()))
                .ifPresent(c -> { throw new ConflictException("CPF já cadastrado"); });
        clientes.findByRg(r.rg()).filter(c -> !c.getId().equals(cliente.getId()))
                .ifPresent(c -> { throw new ConflictException("RG já cadastrado"); });

        List<Rendimento> rendimentos = r.rendimentos() == null ? List.of()
                : r.rendimentos().stream()
                        .map(d -> new Rendimento(d.entidadeEmpregadora(), d.valor())).toList();
        cliente.atualizarDados(r.nome(), r.rg(), r.cpf(), r.endereco(), r.profissao(), rendimentos);
        return ClienteResponse.de(cliente);
    }

    /** Exclui o cadastro e o usuário de acesso. Bloqueado se houver pedidos ou automóveis vinculados. */
    @Transactional
    public void excluir(String email) {
        Cliente cliente = logado.cliente(email);
        if (alugueis.existsByClienteId(cliente.getId())) {
            throw new ConflictException("Seu cadastro possui pedidos de aluguel e não pode ser excluído.");
        }
        if (!automoveis.findByClienteId(cliente.getId()).isEmpty()) {
            throw new ConflictException("Remova seus automóveis antes de excluir o cadastro.");
        }
        tokens.deleteByUsuarioId(cliente.getUsuario().getId());
        clientes.delete(cliente);
    }
}
