package br.com.carro.aluguel.util;

import java.util.Locale;

public final class Entradas {

    private Entradas() {
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
