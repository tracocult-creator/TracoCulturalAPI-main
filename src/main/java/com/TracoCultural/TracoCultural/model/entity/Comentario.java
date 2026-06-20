package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texto", length = 500, nullable = false)
    private String texto;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "id_usuario_fk", nullable = false)
    private Long idUsuarioFk;

    @Column(name = "id_evento_fk", nullable = false)
    private Long idEventoFk;

    // Não existe coluna correspondente no banco — preenchido em memória
    // pelo Service ao montar a resposta, nunca persistido.
    @Transient
    private String nomeUsuario;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }



    //              -------------------------------- Getter e Setter --------------------------------



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Long getIdUsuarioFk() {
        return idUsuarioFk;
    }

    public void setIdUsuarioFk(Long idUsuarioFk) {
        this.idUsuarioFk = idUsuarioFk;
    }

    public Long getIdEventoFk() {
        return idEventoFk;
    }

    public void setIdEventoFk(Long idEventoFk) {
        this.idEventoFk = idEventoFk;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
}