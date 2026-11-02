package br.com.carro.aluguel.dto;

import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @NotBlank String token,
        @NotBlank String senha,
        @NotBlank String confirmacaoSenha) {
}
