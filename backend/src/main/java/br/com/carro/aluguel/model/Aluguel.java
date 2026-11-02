package br.com.carro.aluguel.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alugueis")
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id", nullable = false)
    private Automovel automovel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAluguel status = StatusAluguel.PENDENTE;

    /** Banco que fez a análise financeira (aprovou ou reprovou). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliador_id")
    private Agente avaliador;

    /** Justificativa da recusa/reprovação ou observação da aprovação. */
    @Column(length = 500)
    private String motivo;

    private LocalDateTime dataAvaliacao;

    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }

    public Agente getAvaliador() { return avaliador; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void registrarAvaliacao(Agente avaliador, String motivo) {
        this.avaliador = avaliador;
        this.motivo = motivo == null || motivo.isBlank() ? null : motivo.trim();
        this.dataAvaliacao = LocalDateTime.now();
    }

    public Automovel getAutomovel() { return automovel; }
    public void setAutomovel(Automovel automovel) { this.automovel = automovel; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public StatusAluguel getStatus() { return status; }
    public void setStatus(StatusAluguel status) { this.status = status; }
}