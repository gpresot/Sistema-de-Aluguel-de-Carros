package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.TipoProprietario;
import java.math.BigDecimal;

public record AutomovelResponse(
        Long id,
        String matricula,
        int ano,
        String marca,
        String modelo,
        String placa,
        BigDecimal valorDiaria,
        TipoProprietario tipoProprietario,
        String proprietarioNome,
        String fotoUrl
) {
    /** Chamar dentro de uma transação: lê o proprietário, que é carregado sob demanda. */
    public static AutomovelResponse de(Automovel a) {
        String proprietario = a.getCliente() != null ? a.getCliente().getNome() : a.getAgente().getNome();
        String foto = a.getFotoVersao() == null ? null
                : "/api/automoveis/" + a.getId() + "/foto?v=" + a.getFotoVersao();
        return new AutomovelResponse(a.getId(), a.getMatricula(), a.getAno(), a.getMarca(), a.getModelo(),
                a.getPlaca(), a.getValorDiaria(), a.getTipoProprietario(), proprietario, foto);
    }
}
