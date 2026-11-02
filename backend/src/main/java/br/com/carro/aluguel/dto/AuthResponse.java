package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Perfil;

public record AuthResponse(String token, String email, Perfil perfil, String nome) {
}
