package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Aluguel;
import br.com.carro.aluguel.model.ContratoCredito;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record AluguelResponse(
        Long id,
        String status,
        LocalDate dataInicio,
        LocalDate dataFim,
        long dias,
        BigDecimal valorTotal,
        String motivo,
        String avaliadorNome,
        LocalDateTime dataAvaliacao,
        LocalDateTime criadoEm,
        String clienteNome,
        AutomovelResponse automovel,
        AnaliseFinanceiraResponse analise,
        ContratoCreditoResponse contratoCredito
) {
    /** Resultado da análise de renda feita pelo banco. */
    public record AnaliseFinanceiraResponse(BigDecimal rendaMensal, BigDecimal parcelaMensal,
                                            BigDecimal limiteParcela, boolean aprovada) {
    }

    /** Contrato de crédito vinculado ao pedido. */
    public record ContratoCreditoResponse(Long id, String bancoNome, BigDecimal valorFinanciado,
                                          BigDecimal taxaJurosMensal, LocalDateTime criadoEm) {
        static ContratoCreditoResponse de(ContratoCredito c) {
            return c == null ? null : new ContratoCreditoResponse(c.getId(), c.getBanco().getNome(),
                    c.getValorFinanciado(), c.getTaxaJurosMensal(), c.getCriadoEm());
        }
    }

    /** Chamar dentro de uma transação (acessa associações carregadas sob demanda). */
    public static AluguelResponse de(Aluguel a, AnaliseFinanceiraResponse analise) {
        long dias = ChronoUnit.DAYS.between(a.getDataInicio(), a.getDataFim()) + 1;
        return new AluguelResponse(
                a.getId(),
                a.getStatus().name(),
                a.getDataInicio(),
                a.getDataFim(),
                dias,
                a.getAutomovel().getValorDiaria().multiply(BigDecimal.valueOf(dias)),
                a.getMotivo(),
                a.getAvaliador() == null ? null : a.getAvaliador().getNome(),
                a.getDataAvaliacao(),
                a.getCriadoEm(),
                a.getCliente().getNome(),
                AutomovelResponse.de(a.getAutomovel()),
                analise,
                ContratoCreditoResponse.de(a.getContratoCredito()));
    }
}
