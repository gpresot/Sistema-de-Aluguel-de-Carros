package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.TipoProprietario;

public record AutomovelResponse(
        Long id,
        String matricula,
        int ano,
        String marca,
        String modelo,
        String placa,
        TipoProprietario tipoProprietario
) {
    public static AutomovelResponse de(Automovel automovel) {
        return new AutomovelResponse(
                automovel.getId(),
                automovel.getMatricula(),
                automovel.getAno(),
                automovel.getMarca(),
                automovel.getModelo(),
                automovel.getPlaca(),
                automovel.getTipoProprietario()
        );
    }
}