package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.FavoritoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Favorito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;


    public Favorito favoritar(Long usuarioId, Long eventoId) {
        if (favoritoRepository.existsByUsuarioIdAndEventoId(usuarioId, eventoId)) {
            throw new RuntimeException("Evento já está nos favoritos");
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + usuarioId)));
        favorito.setEvento(eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com o ID: " + eventoId)));

        return favoritoRepository.save(favorito);
    }

    public void desfavoritar(Long usuarioId, Long eventoId) {
        Favorito favorito = favoritoRepository.findByUsuarioIdAndEventoId(usuarioId, eventoId)
                .orElseThrow(() -> new RuntimeException("Favorito não encontrado"));
        favoritoRepository.delete(favorito);
    }

    public List<Favorito> listarPorUsuario(Long usuarioId) {
        return favoritoRepository.findByUsuarioId(usuarioId);
    }
}
