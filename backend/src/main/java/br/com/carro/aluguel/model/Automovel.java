
package br.com.carro.aluguel.model;

import jakarta.persistence.*;
import java.time.Year;
import java.math.BigDecimal;

@Entity
@Table(name = "automoveis", uniqueConstraints = {
        @UniqueConstraint(columnNames = "matricula"),
        @UniqueConstraint(columnNames = "placa")
})
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String matricula;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProprietario tipoProprietario;
    @Column(nullable = false, precision = 14, scale = 2, columnDefinition = "numeric(14,2) not null default 0")
    private BigDecimal valorDiaria = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    protected Automovel() {
    }

    public Automovel(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            Cliente cliente) {
        validarDados(matricula, ano, marca, modelo, placa);

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "O proprietário cliente é obrigatório.");
        }

        this.matricula = matricula.trim();
        this.ano = ano;
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.placa = placa.trim().toUpperCase();
        this.tipoProprietario = TipoProprietario.CLIENTE;
        this.cliente = cliente;
        this.agente = null;
    }

    public Automovel(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            Agente agente) {
        validarDados(matricula, ano, marca, modelo, placa);

        if (agente == null) {
            throw new IllegalArgumentException(
                    "O proprietário agente é obrigatório.");
        }

        this.matricula = matricula.trim();
        this.ano = ano;
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.placa = placa.trim().toUpperCase();
        this.tipoProprietario = agente.getUsuario().getPerfil() == Perfil.BANCO
                ? TipoProprietario.BANCO
                : TipoProprietario.EMPRESA;
        this.agente = agente;
        this.cliente = null;
    }

    private void validarDados(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }

        if (ano < 1886 || ano > Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Ano do automóvel inválido.");
        }

        if (marca == null || marca.isBlank()) {
            throw new IllegalArgumentException("A marca é obrigatória.");
        }

        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("O modelo é obrigatório.");
        }

        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("A placa é obrigatória.");
        }
    }

    public void atualizarDados(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa) {
        validarDados(matricula, ano, marca, modelo, placa);

        this.matricula = matricula.trim();
        this.ano = ano;
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.placa = placa.trim().toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public int getAno() {
        return ano;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoProprietario getTipoProprietario() {
        return tipoProprietario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Agente getAgente() {
        return agente;
    }
        public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void definirValorDiaria(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("O valor da diária não pode ser negativo.");
        }
        this.valorDiaria = valor;
    }
}
