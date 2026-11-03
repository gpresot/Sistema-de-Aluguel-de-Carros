package br.com.carro.aluguel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Contrato de crédito concedido por um banco agente a um pedido de aluguel aprovado. */
@Entity
@Table(name = "contratos_credito")
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluguel_id", nullable = false, unique = true)
    private Aluguel aluguel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Agente banco;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorFinanciado;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal taxaJurosMensal;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected ContratoCredito() {
    }

    public ContratoCredito(Aluguel aluguel, Agente banco, BigDecimal valorFinanciado, BigDecimal taxaJurosMensal) {
        if (banco.getUsuario().getPerfil() != Perfil.BANCO) {
            throw new IllegalArgumentException("Somente agentes com perfil BANCO podem conceder crédito.");
        }
        if (valorFinanciado == null || valorFinanciado.signum() <= 0) {
            throw new IllegalArgumentException("O valor financiado deve ser maior que zero.");
        }
        if (taxaJurosMensal == null || taxaJurosMensal.signum() < 0 || taxaJurosMensal.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("A taxa de juros mensal deve estar entre 0 e 100.");
        }
        this.aluguel = aluguel;
        this.banco = banco;
        this.valorFinanciado = valorFinanciado;
        this.taxaJurosMensal = taxaJurosMensal;
    }

    public Long getId() { return id; }
    public Aluguel getAluguel() { return aluguel; }
    public Agente getBanco() { return banco; }
    public BigDecimal getValorFinanciado() { return valorFinanciado; }
    public BigDecimal getTaxaJurosMensal() { return taxaJurosMensal; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
