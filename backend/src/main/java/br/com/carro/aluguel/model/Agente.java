package br.com.carro.aluguel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agentes")
public class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    protected Agente() {
    }

    public Agente(String nome, String cnpj, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.CLIENTE) {
            throw new IllegalArgumentException("Agente exige usuário com perfil EMPRESA ou BANCO");
        }
        this.nome = nome;
        this.cnpj = cnpj;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public Usuario getUsuario() { return usuario; }
}
