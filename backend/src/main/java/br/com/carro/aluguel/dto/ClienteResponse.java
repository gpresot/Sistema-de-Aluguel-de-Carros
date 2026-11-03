package br.com.carro.aluguel.dto;

import br.com.carro.aluguel.model.Cliente;
import java.util.List;

public record ClienteResponse(
        Long id,
        String email,
        String nome,
        String rg,
        String cpf,
        String endereco,
        String profissao,
        List<RendimentoDto> rendimentos) {

    /** Chamar dentro de uma transação (os rendimentos são carregados sob demanda). */
    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getUsuario().getEmail(), c.getNome(), c.getRg(), c.getCpf(),
                c.getEndereco(), c.getProfissao(),
                c.getRendimentos().stream()
                        .map(r -> new RendimentoDto(r.getEntidadeEmpregadora(), r.getValor())).toList());
    }
}
