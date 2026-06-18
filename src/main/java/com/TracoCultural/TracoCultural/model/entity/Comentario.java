package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idUsuarioFk", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idEventoFk", nullable = false)
    private Evento evento;

    @Column(length = 500, nullable = false)
    private String texto;

    @Column(name = "dataCriacao", nullable = false)
    private Date dataCriacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
}
