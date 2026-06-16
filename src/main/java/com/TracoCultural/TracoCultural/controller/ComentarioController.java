package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Comentario;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EventoRepository eventoRepository;

    @GetMapping
    public ResponseEntity<List<Comentario>> listar(@RequestParam Long eventoId) {
        return ResponseEntity.ok(comentarioRepository.findByEventoId(eventoId));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody Map<String, Object> body) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(email);

        Long eventoId = Long.valueOf(body.get("eventoId").toString());
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        comentario.setEvento(evento);
        comentario.setTexto(body.get("texto").toString());
        comentario.setDataCriacao(new Date());

        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioRepository.save(comentario));
    }
}
