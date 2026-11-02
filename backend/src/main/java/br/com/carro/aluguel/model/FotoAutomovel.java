package br.com.carro.aluguel.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Foto guardada à parte para que listar automóveis não carregue os bytes das imagens. */
@Entity
@Table(name = "fotos_automovel")
public class FotoAutomovel {

    @Id
    private Long automovelId;

    @Column(nullable = false, length = 30)
    private String contentType;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] conteudo;

    protected FotoAutomovel() {
    }

    public FotoAutomovel(Long automovelId, String contentType, byte[] conteudo) {
        this.automovelId = automovelId;
        this.contentType = contentType;
        this.conteudo = conteudo;
    }

    public void substituir(String contentType, byte[] conteudo) {
        this.contentType = contentType;
        this.conteudo = conteudo;
    }

    public Long getAutomovelId() { return automovelId; }
    public String getContentType() { return contentType; }
    public byte[] getConteudo() { return conteudo; }
}
