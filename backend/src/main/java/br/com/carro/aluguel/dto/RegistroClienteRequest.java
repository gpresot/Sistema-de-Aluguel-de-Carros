package br.com.carro.aluguel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RegistroClienteRequest(
        @NotBlank @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Informe um email válido.") String email,
        @NotBlank String senha,
        @NotBlank String confirmacaoSenha,
        @NotBlank String nome,
        @NotBlank String rg,
        @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos, apenas números") String cpf,
        @NotBlank String endereco,
        @NotBlank String profissao,
        @Valid @Size(max = 3, message = "No máximo 3 rendimentos") List<RendimentoDto> rendimentos) {
}
