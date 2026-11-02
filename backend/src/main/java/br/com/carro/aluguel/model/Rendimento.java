package br.com.carro.aluguel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Rendimento {

    @Column(nullable = false)
    private String entidadeEmpregadora;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    protected Rendimento() {
    }

    public Rendimento(String entidadeEmpregadora, BigDecimal valor) {
        this.entidadeEmpregadora = entidadeEmpregadora;
        this.valor = valor;
    }

    public String getEntidadeEmpregadora() { return entidadeEmpregadora; }
    public BigDecimal getValor() { return valor; }
}
