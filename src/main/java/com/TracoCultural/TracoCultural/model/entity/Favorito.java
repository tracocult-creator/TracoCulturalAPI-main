package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Favorito")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idUsuarioFk", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idEventoFk", nullable = false)
    private Evento evento;



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
}
