package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.entity.Comentario;
import com.TracoCultural.TracoCultural.model.services.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @GetMapping
    public ResponseEntity<List<Comentario>> listar(@PathVariable Long eventoId) {
        return ResponseEntity.ok(comentarioService.listarPorEvento(eventoId));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@PathVariable Long eventoId,
                                         @RequestBody Map<String, String> body,
                                         Authentication auth) {
        try {
            Comentario comentario = comentarioService.salvar(eventoId, body.get("texto"), auth);
            return ResponseEntity.status(201).body(comentario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Object> deletar(@PathVariable Long eventoId,
                                           @PathVariable Long comentarioId,
                                           Authentication auth) {
        try {
            comentarioService.deletar(comentarioId, auth);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Comentário removido com sucesso."));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("status", 403, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }
}