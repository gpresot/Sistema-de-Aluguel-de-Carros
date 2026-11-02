package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Aluguel;
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
        AnaliseFinanceiraResponse analise
) {
    /** Resultado da análise de renda feita pelo banco. */
    public record AnaliseFinanceiraResponse(BigDecimal rendaMensal, BigDecimal parcelaMensal,
                                            BigDecimal limiteParcela, boolean aprovada) {
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
                analise);
    }
}
