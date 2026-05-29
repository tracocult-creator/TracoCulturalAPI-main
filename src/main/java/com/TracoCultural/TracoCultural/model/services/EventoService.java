package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;


    public List<Evento> findAll() {
        return eventoRepository.findAll();
    }

    public List<Evento> findByCidade(String cidade) {
        return eventoRepository.findByCidadeIgnoreCase(cidade);
    }

    public List<Evento> findByCategoria(Long categoriaId) {
        return eventoRepository.findByCategoriaId(categoriaId);
    }

    public List<Evento> findByCidadeAndCategoria(String cidade, Long categoriaId) {
        return eventoRepository.findByCidadeIgnoreCaseAndCategoriaId(cidade, categoriaId);
    }

    public Evento findById(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com o ID: " + id));
    }

    public Evento save(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Evento update(Long id, Evento evento) {
        Evento existente = findById(id);
        existente.setNome(evento.getNome());
        existente.setDescricao(evento.getDescricao());
        existente.setDataInicio(evento.getDataInicio());
        existente.setDataFim(evento.getDataFim());
        existente.setCidade(evento.getCidade());
        existente.setLinkExterno(evento.getLinkExterno());
        existente.setUsuario(evento.getUsuario());          // ← era setIdUsuarioFk(Long)
        existente.setCategoria(evento.getCategoria());
        if (evento.getCardImage() != null) {
            existente.setCardImage(evento.getCardImage());
        }
        return eventoRepository.save(existente);
    }

    public void deleteById(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new RuntimeException("Evento não encontrado com o ID: " + id);
        }
        eventoRepository.deleteById(id);
    }
}