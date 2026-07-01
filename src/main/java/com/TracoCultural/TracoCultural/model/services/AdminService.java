package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.dto.UsuarioDTO;
import com.TracoCultural.TracoCultural.model.entity.Comentario;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private ComentarioRepository comentarioRepository;

    // ── Dashboard ──────────────────────────────────────────────────────────────

    public Map<String, Long> dashboard() {
        long usuarios       = usuarioRepository.count();
        long administradores = usuarioRepository.findAll().stream().filter(Usuario::getIsAdm).count();
        long eventos        = eventoRepository.count();
        long comentarios    = comentarioRepository.count();
        long destacados     = eventoRepository.findAll().stream()
                                .filter(e -> Boolean.TRUE.equals(e.getDestacado())).count();
        return Map.of(
            "usuarios",          usuarios,
            "administradores",   administradores,
            "eventos",           eventos,
            "comentarios",       comentarios,
            "eventosDestacados", destacados
        );
    }

    // ── Usuários ───────────────────────────────────────────────────────────────

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(UsuarioDTO::new).toList();
    }

    @Transactional
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id))
            throw new RuntimeException("Usuário não encontrado");
        List<Evento> eventos = eventoRepository.findByIdUsuarioFk(id);
        for (Evento ev : eventos)
            comentarioRepository.deleteByIdEventoFk(ev.getId());
        eventoRepository.deleteByIdUsuarioFk(id);
        usuarioRepository.deleteById(id);
    }

    public UsuarioDTO alternarAdmin(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        u.setIsAdm(!u.getIsAdm());
        return new UsuarioDTO(usuarioRepository.save(u));
    }

    // ── Eventos ────────────────────────────────────────────────────────────────

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    public Evento editarEvento(Long id, Evento dados) {
        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (dados.getNome()       != null) existente.setNome(dados.getNome());
        if (dados.getDescricao()  != null) existente.setDescricao(dados.getDescricao());
        if (dados.getCidade()     != null) existente.setCidade(dados.getCidade());
        if (dados.getDataInicio() != null) existente.setDataInicio(dados.getDataInicio());
        if (dados.getDataFim()    != null) existente.setDataFim(dados.getDataFim());
        if (dados.getLinkExterno()!= null) existente.setLinkExterno(dados.getLinkExterno());
        if (dados.getCategoria()  != null) existente.setCategoria(dados.getCategoria());
        if (dados.getCardImage()  != null) existente.setCardImage(dados.getCardImage());
        if (dados.getDestacado()  != null) existente.setDestacado(dados.getDestacado());
        if (dados.getPatrocinado()!= null) existente.setPatrocinado(dados.getPatrocinado());

        return eventoRepository.save(existente);
    }

    @Transactional
    public void deletarEvento(Long id) {
        if (!eventoRepository.existsById(id))
            throw new RuntimeException("Evento não encontrado");
        comentarioRepository.deleteByIdEventoFk(id);
        eventoRepository.deleteById(id);
    }

    public Evento destacarEvento(Long id) {
        Evento e = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        e.setDestacado(!Boolean.TRUE.equals(e.getDestacado()));
        return eventoRepository.save(e);
    }

    public Evento patrocinarEvento(Long id) {
        Evento e = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        e.setPatrocinado(!Boolean.TRUE.equals(e.getPatrocinado()));
        return eventoRepository.save(e);
    }

    // ── Comentários ────────────────────────────────────────────────────────────

    public List<Comentario> listarComentarios() {
        return comentarioRepository.findAll();
    }

    public void deletarComentario(Long id) {
        if (!comentarioRepository.existsById(id))
            throw new RuntimeException("Comentário não encontrado");
        comentarioRepository.deleteById(id);
    }
}
