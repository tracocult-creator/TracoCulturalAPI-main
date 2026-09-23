package com.TracoCultural.TracoCultural.model.dto;

import com.TracoCultural.TracoCultural.model.entity.Evento;

/**
 * Detalhe de um evento incluindo informações de favoritos:
 * - totalFavoritos: quantos usuários favoritaram esse evento
 * - favoritadoPeloUsuario: se o usuário autenticado na requisição já
 *   favoritou esse evento (sempre false se ninguém estiver logado, já
 *   que GET /eventos/{id} é público)
 */
public class EventoDetalheDTO {

    private Evento evento;
    private long totalFavoritos;
    private boolean favoritadoPeloUsuario;

    public EventoDetalheDTO(Evento evento, long totalFavoritos, boolean favoritadoPeloUsuario) {
        this.evento = evento;
        this.totalFavoritos = totalFavoritos;
        this.favoritadoPeloUsuario = favoritadoPeloUsuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public long getTotalFavoritos() {
        return totalFavoritos;
    }

    public void setTotalFavoritos(long totalFavoritos) {
        this.totalFavoritos = totalFavoritos;
    }

    public boolean isFavoritadoPeloUsuario() {
        return favoritadoPeloUsuario;
    }

    public void setFavoritadoPeloUsuario(boolean favoritadoPeloUsuario) {
        this.favoritadoPeloUsuario = favoritadoPeloUsuario;
    }
}
