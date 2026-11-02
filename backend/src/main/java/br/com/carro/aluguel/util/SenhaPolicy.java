package br.com.carro.aluguel.util;

import br.com.carro.aluguel.exception.BadRequestException;
import java.nio.charset.StandardCharsets;

/** Regras de senha: mínimo de 8 caracteres, máximo de 72 bytes (limite do BCrypt) e confirmação igual. */
public final class SenhaPolicy {

    private SenhaPolicy() {
    }

    public static void validar(String senha, String confirmacao) {
        if (senha == null || senha.isBlank() || senha.length() < 8) {
            throw new BadRequestException("A senha deve possuir pelo menos 8 caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("A senha deve ter no máximo 72 bytes.");
        }
        if (!senha.equals(confirmacao)) {
            throw new BadRequestException("As senhas informadas não coincidem.");
        }
    }
}
