package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.dto.AluguelResponse;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.service.AluguelService;
import br.com.carro.aluguel.service.UsuarioLogadoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alugueis")
public class AluguelController {

    private final AluguelService service;
    private final UsuarioLogadoService logado;

    public AluguelController(AluguelService service, UsuarioLogadoService logado) {
        this.service = service;
        this.logado = logado;
    }

    public record PedidoAluguelRequest(Long automovelId, LocalDate dataInicio, LocalDate dataFim) {}

    public record DatasRequest(LocalDate dataInicio, LocalDate dataFim) {}

    public record RecusaRequest(String motivo) {}

    public record ParecerRequest(Boolean aprovar, String motivo) {}

    public record CreditoRequest(BigDecimal valorFinanciado, BigDecimal taxaJurosMensal) {}

    // Cliente: cria pedido (nasce PENDENTE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AluguelResponse criar(@RequestBody PedidoAluguelRequest req, Authentication auth) {
        return service.criarPedido(usuario(auth), req.automovelId(), req.dataInicio(), req.dataFim());
    }

    // Cliente: lista os próprios pedidos
    @GetMapping("/meus")
    public List<AluguelResponse> listarMeus(Authentication auth) {
        return service.listarDoCliente(usuario(auth));
    }

    // Empresa: pedidos dos seus carros. Banco: pedidos em análise e os já avaliados por ele
    @GetMapping("/agente")
    public List<AluguelResponse> listarDoAgente(Authentication auth) {
        return service.listarDoAgente(usuario(auth));
    }

    // Empresa: aceita o pedido (PENDENTE -> EM_ANALISE, segue para o banco)
    @PutMapping("/{id}/aceitar")
    public AluguelResponse aceitar(@PathVariable Long id, Authentication auth) {
        return service.aceitarPedido(usuario(auth), id);
    }

    // Empresa: recusa o pedido (PENDENTE -> REJEITADO)
    @PutMapping("/{id}/recusar")
    public AluguelResponse recusar(@PathVariable Long id, @RequestBody(required = false) RecusaRequest req,
                                   Authentication auth) {
        return service.recusarPedido(usuario(auth), id, req == null ? null : req.motivo());
    }

    // Banco: análise de renda e parecer (EM_ANALISE -> APROVADO ou REJEITADO)
    @PutMapping("/{id}/parecer")
    public AluguelResponse parecer(@PathVariable Long id, @RequestBody ParecerRequest req, Authentication auth) {
        return service.parecerFinanceiro(usuario(auth), id, Boolean.TRUE.equals(req.aprovar()), req.motivo());
    }

    // Banco: concede contrato de crédito a um pedido aprovado
    @PostMapping("/{id}/credito")
    @ResponseStatus(HttpStatus.CREATED)
    public AluguelResponse concederCredito(@PathVariable Long id, @RequestBody CreditoRequest req, Authentication auth) {
        return service.concederCredito(usuario(auth), id, req.valorFinanciado(), req.taxaJurosMensal());
    }

    // Agente autorizado: executa o contrato de um pedido aprovado (APROVADO -> CONTRATADO)
    @PutMapping("/{id}/contrato")
    public AluguelResponse executarContrato(@PathVariable Long id, Authentication auth) {
        return service.executarContrato(usuario(auth), id);
    }

    // Cliente, empresa (pedido pendente) ou banco (pedido em análise): altera datas
    @PutMapping("/{id}/datas")
    public AluguelResponse alterarDatas(@PathVariable Long id, @RequestBody DatasRequest req, Authentication auth) {
        return service.alterarDatas(usuario(auth), id, req.dataInicio(), req.dataFim());
    }

    // Cliente: cancela (mantém o registro com status CANCELADO)
    @DeleteMapping("/{id}")
    public AluguelResponse cancelar(@PathVariable Long id, Authentication auth) {
        return service.cancelarPedido(usuario(auth), id);
    }

    private Usuario usuario(Authentication auth) {
        return logado.usuario(auth.getName());
    }
}
