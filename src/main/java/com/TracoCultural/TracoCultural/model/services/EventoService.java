package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class EventoService {

    private static final int LIMITE_IMAGEM_BYTES = 2 * 1024 * 1024; // 2MB

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


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
        if (evento.getCardImage() != null && evento.getCardImage().length > LIMITE_IMAGEM_BYTES)
            throw new IllegalArgumentException("Imagem deve ter no máximo 2MB.");

        if (evento.getDataFim() != null && !evento.getDataFim().after(evento.getDataInicio()))
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início.");

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(email);
        evento.setIdUsuarioFk(usuario.getId());

        return eventoRepository.save(evento);
    }

    public Evento update(Long id, Evento evento) {
        Evento existente = findById(id);

        if (evento.getCardImage() != null && evento.getCardImage().length > LIMITE_IMAGEM_BYTES)
            throw new IllegalArgumentException("Imagem deve ter no máximo 2MB.");

        Date dataInicioFinal = evento.getDataInicio() != null ? evento.getDataInicio() : existente.getDataInicio();
        Date dataFimFinal = evento.getDataFim() != null ? evento.getDataFim() : existente.getDataFim();

        if (dataFimFinal != null && !dataFimFinal.after(dataInicioFinal))
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início.");

        // Atualização parcial: só sobrescreve o que veio preenchido no payload,
        // preservando nome/cidade/categoria/imagem existentes quando omitidos
        // (o formulário de edição do front web e do mobile não reenvia todos os campos).
        if (evento.getNome() != null && !evento.getNome().isBlank()) existente.setNome(evento.getNome());
        existente.setDescricao(evento.getDescricao());
        existente.setDataInicio(dataInicioFinal);
        existente.setDataFim(dataFimFinal);
        if (evento.getCidade() != null && !evento.getCidade().isBlank()) existente.setCidade(evento.getCidade());
        existente.setLinkExterno(evento.getLinkExterno());
        if (evento.getCategoria() != null) existente.setCategoria(evento.getCategoria());
        if (evento.getCardImage() != null) existente.setCardImage(evento.getCardImage());
        return eventoRepository.save(existente);
    }

    public void deleteById(Long id) {
        if (!eventoRepository.existsById(id))
            throw new RuntimeException("Evento não encontrado com o ID: " + id);
        eventoRepository.deleteById(id);
    }

    public List<Evento> findByUsuarioId(Long id) {
        return eventoRepository.findByIdUsuarioFk(id);
    }
}