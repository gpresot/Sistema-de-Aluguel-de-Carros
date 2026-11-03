package br.com.carro.aluguel.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    public static final int MAX_RENDIMENTOS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String rg;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String profissao;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ElementCollection
    @CollectionTable(name = "cliente_rendimentos", joinColumns = @JoinColumn(name = "cliente_id"))
    private List<Rendimento> rendimentos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nome, String rg, String cpf, String endereco, String profissao,
                   Usuario usuario, List<Rendimento> rendimentos) {
        if (rendimentos != null && rendimentos.size() > MAX_RENDIMENTOS) {
            throw new IllegalArgumentException("Um cliente pode ter no máximo " + MAX_RENDIMENTOS + " rendimentos");
        }
        this.nome = nome;
        this.rg = rg;
        this.cpf = cpf;
        this.endereco = endereco;
        this.profissao = profissao;
        this.usuario = usuario;
        if (rendimentos != null) {
            this.rendimentos = new ArrayList<>(rendimentos);
        }
    }

    /** Atualiza os dados cadastrais; os rendimentos informados substituem os atuais (máximo 3). */
    public void atualizarDados(String nome, String rg, String cpf, String endereco, String profissao,
                               List<Rendimento> novosRendimentos) {
        if (novosRendimentos != null && novosRendimentos.size() > MAX_RENDIMENTOS) {
            throw new IllegalArgumentException("Um cliente pode ter no máximo " + MAX_RENDIMENTOS + " rendimentos");
        }
        this.nome = nome;
        this.rg = rg;
        this.cpf = cpf;
        this.endereco = endereco;
        this.profissao = profissao;
        this.rendimentos.clear();
        if (novosRendimentos != null) {
            this.rendimentos.addAll(novosRendimentos);
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getRg() { return rg; }
    public String getCpf() { return cpf; }
    public String getEndereco() { return endereco; }
    public String getProfissao() { return profissao; }
    public Usuario getUsuario() { return usuario; }
    public List<Rendimento> getRendimentos() { return rendimentos; }
}
