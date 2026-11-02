
package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.Cliente;
import br.com.carro.aluguel.model.Perfil;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import br.com.carro.aluguel.service.AutomovelService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import br.com.carro.aluguel.dto.AutomovelResponse;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelService automovelService;
    private final AutomovelRepository automoveis;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;
    private final UsuarioRepository usuarios;

    public AutomovelController(
            AutomovelService automovelService,
            AutomovelRepository automoveis,
            ClienteRepository clientes,
            AgenteRepository agentes,
            UsuarioRepository usuarios
    ) {
        this.automovelService = automovelService;
        this.automoveis = automoveis;
        this.clientes = clientes;
        this.agentes = agentes;
        this.usuarios = usuarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse cadastrar(
            @RequestBody AutomovelRequest request,
            Authentication authentication
    ) {
        Usuario usuario = obterUsuario(authentication);

        if (usuario.getPerfil() == Perfil.CLIENTE) {
            Cliente cliente = clientes.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Cliente não encontrado."
                    ));

            return AutomovelResponse.de(
        automovelService.cadastrarParaCliente(
                request.matricula(), request.ano(), request.marca(),
                request.modelo(), request.placa(), cliente.getId()
        )
);
        }

        if (usuario.getPerfil() == Perfil.EMPRESA
                || usuario.getPerfil() == Perfil.BANCO) {

            Agente agente = agentes.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Agente não encontrado."
                    ));

            return AutomovelResponse.de(
        automovelService.cadastrarParaAgente(
                request.matricula(), request.ano(), request.marca(),
                request.modelo(), request.placa(), agente.getId()
        )
);
        }

        throw new AccessDeniedException("Perfil sem permissão.");
    }

    @GetMapping
    public List<AutomovelResponse> listar(Authentication authentication) {
        Usuario usuario = obterUsuario(authentication);

        if (usuario.getPerfil() == Perfil.CLIENTE) {
            Cliente cliente = clientes.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Cliente não encontrado."
                    ));

            return automovelService.listarPorCliente(cliente.getId())
        .stream()
        .map(AutomovelResponse::de)
        .toList();
        }

        Agente agente = agentes.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agente não encontrado."
                ));

        return automovelService.listarPorAgente(agente.getId())
        .stream()
        .map(AutomovelResponse::de)
        .toList();
    }

    @GetMapping("/{id}")
    public AutomovelResponse buscar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Automovel automovel = automovelService.buscarPorId(id);
        verificarProprietario(automovel, authentication);
        return AutomovelResponse.de(automovel);
    }

    @PutMapping("/{id}")
    public AutomovelResponse atualizar(
            @PathVariable Long id,
            @RequestBody AutomovelRequest request,
            Authentication authentication
    ) {
        Automovel automovel = automovelService.buscarPorId(id);
        verificarProprietario(automovel, authentication);

        return AutomovelResponse.de(
        automovelService.atualizar(
                id, request.matricula(), request.ano(),
                request.marca(), request.modelo(), request.placa()
        )
);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Automovel automovel = automovelService.buscarPorId(id);
        verificarProprietario(automovel, authentication);
        automovelService.excluir(id);
    }

    private Usuario obterUsuario(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        return usuarios.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário não encontrado."
                ));
    }

    private void verificarProprietario(
            Automovel automovel,
            Authentication authentication
    ) {
        Usuario usuario = obterUsuario(authentication);

        boolean pertenceAoUsuario;

        if (usuario.getPerfil() == Perfil.CLIENTE) {
            pertenceAoUsuario = automovel.getCliente() != null
                    && automovel.getCliente().getUsuario().getId()
                    .equals(usuario.getId());
        } else {
            pertenceAoUsuario = automovel.getAgente() != null
                    && automovel.getAgente().getUsuario().getId()
                    .equals(usuario.getId());
        }

        if (!pertenceAoUsuario) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este automóvel."
            );
        }
    }

    public record AutomovelRequest(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa
    ) {
    }
}
