package br.com.carro.aluguel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AutomovelRequest(
        @NotBlank(message = "A matrícula é obrigatória.") String matricula,
        @Min(value = 1886, message = "Ano do automóvel inválido.") int ano,
        @NotBlank(message = "A marca é obrigatória.") String marca,
        @NotBlank(message = "O modelo é obrigatório.") String modelo,
        @NotBlank(message = "A placa é obrigatória.") String placa,
        @NotNull(message = "Informe o valor da diária.")
        @Positive(message = "O valor da diária deve ser maior que zero.") BigDecimal valorDiaria) {
}
