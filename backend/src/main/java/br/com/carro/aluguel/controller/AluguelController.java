package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.model.Aluguel;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.UsuarioRepository;
import br.com.carro.aluguel.service.AluguelService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/alugueis")
public class AluguelController {

        private final AluguelService aluguelService;
        private final UsuarioRepository usuarios;

        public AluguelController(AluguelService aluguelService, UsuarioRepository usuarios) {
                this.aluguelService = aluguelService;
                this.usuarios = usuarios;
        }

        public record PedidoAluguelRequest(Long automovelId, LocalDate dataInicio, LocalDate dataFim) {}

        public record DatasRequest(LocalDate dataInicio, LocalDate dataFim) {}

        public record AluguelResponse(Long id, Long automovelId, LocalDate dataInicio, LocalDate dataFim, String status) {}

        // Cliente: cria pedido
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public AluguelResponse criar(@RequestBody PedidoAluguelRequest req, Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                Aluguel a = aluguelService.criarPedido(usuario, req.automovelId(), req.dataInicio(), req.dataFim());
                return paraResponse(a);
        }

        // Cliente: lista os próprios pedidos
        @GetMapping("/meus")
        public List<AluguelResponse> listarMeus(Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return aluguelService.listarDoCliente(usuario).stream()
                                .map(AluguelController::paraResponse)
                                .toList();
        }

        // Agente (empresa ou banco): lista pedidos dos seus automóveis
        @GetMapping("/agente")
        public List<AluguelResponse> listarDoAgente(Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return aluguelService.listarDoAgente(usuario).stream()
                                .map(AluguelController::paraResponse)
                                .toList();
        }

        // Agente: coloca o pedido em análise
        @PutMapping("/{id}/analise")
        public AluguelResponse iniciarAnalise(@PathVariable Long id, Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return paraResponse(aluguelService.iniciarAnalise(usuario, id));
        }

        // Agente: aprova (com análise financeira positiva) ou rejeita
        @PutMapping("/{id}/parecer")
        public AluguelResponse darParecer(@PathVariable Long id,
                        @RequestParam boolean aprovar,
                        Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return paraResponse(aluguelService.darParecer(usuario, id, aprovar));
        }

        // Cliente (pendente) ou agente (pendente/em análise): altera datas
        @PutMapping("/{id}/datas")
        public AluguelResponse alterarDatas(@PathVariable Long id,
                        @RequestBody DatasRequest req,
                        Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return paraResponse(aluguelService.alterarDatas(usuario, id, req.dataInicio(), req.dataFim()));
        }

        // Cliente: cancela o pedido (mantém o registro com status CANCELADO)
        @DeleteMapping("/{id}")
        public AluguelResponse cancelar(@PathVariable Long id, Authentication authentication) {
                Usuario usuario = obterUsuario(authentication);
                return paraResponse(aluguelService.cancelarPedido(usuario, id));
        }

        private static AluguelResponse paraResponse(Aluguel a) {
                return new AluguelResponse(
                                a.getId(),
                                a.getAutomovel().getId(),
                                a.getDataInicio(),
                                a.getDataFim(),
                                a.getStatus().name());
        }

        private Usuario obterUsuario(Authentication authentication) {
                if (authentication == null) {
                        throw new AccessDeniedException("Usuário não autenticado.");
                }
                return usuarios.findByEmail(authentication.getName())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        }
}