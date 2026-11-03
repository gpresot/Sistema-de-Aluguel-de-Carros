package br.com.carro.aluguel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Dados cadastrais que o cliente pode alterar (e-mail e senha seguem fluxos próprios). */
public record ClienteRequest(
        @NotBlank String nome,
        @NotBlank String rg,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos, apenas números") String cpf,
        @NotBlank String endereco,
        @NotBlank String profissao,
        @Valid @Size(max = 3, message = "No máximo 3 rendimentos") List<RendimentoDto> rendimentos) {
}
