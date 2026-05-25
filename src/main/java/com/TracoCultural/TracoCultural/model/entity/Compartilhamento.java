package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Compartilhamento")
public class Compartilhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idUsuarioFk", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idEventoFk", nullable = false)
    private Evento evento;

    private Date dataCompartilhamento;



    //              -------------------------------- Getter e Setter --------------------------------


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Date getDataCompartilhamento() {
        return dataCompartilhamento;
    }

    public void setDataCompartilhamento(Date dataCompartilhamento) {
        this.dataCompartilhamento = dataCompartilhamento;
    }
}
