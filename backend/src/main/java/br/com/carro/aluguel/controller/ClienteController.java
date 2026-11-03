package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.dto.ClienteRequest;
import br.com.carro.aluguel.dto.ClienteResponse;
import br.com.carro.aluguel.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** CRUD do cliente: cada cliente acessa apenas o próprio cadastro. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ClienteResponse consultar(Authentication auth) {
        return service.consultar(auth.getName());
    }

    @PutMapping("/me")
    public ClienteResponse atualizar(@Valid @RequestBody ClienteRequest request, Authentication auth) {
        return service.atualizar(auth.getName(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(Authentication auth) {
        service.excluir(auth.getName());
    }
}
