package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EventoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioRepository.findByEmail(email);
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long idUsuario) {
        if (idUsuario != null)
            return ResponseEntity.ok(eventoService.findByUsuarioId(idUsuario));
        if (cidade != null && categoriaId != null)
            return ResponseEntity.ok(eventoService.findByCidadeAndCategoria(cidade, categoriaId));
        if (cidade != null)
            return ResponseEntity.ok(eventoService.findByCidade(cidade));
        if (categoriaId != null)
            return ResponseEntity.ok(eventoService.findByCategoria(categoriaId));
        return ResponseEntity.ok(eventoService.findAll());
    }

    @GetMapping("/meus")
    public ResponseEntity<List<Evento>> meusEventos() {
        return ResponseEntity.ok(eventoService.findByUsuarioId(getUsuarioAutenticado().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(eventoService.findById(Long.parseLong(id)));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "O id informado não é válido: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Object> publicarEvento(@Valid @RequestBody Evento evento) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.save(evento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarEvento(@PathVariable String id, @Valid @RequestBody Evento evento) {
        try {
            Evento existente = eventoService.findById(Long.parseLong(id));
            if (!existente.getIdUsuarioFk().equals(getUsuarioAutenticado().getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));
            }
            return ResponseEntity.ok(eventoService.update(Long.parseLong(id), evento));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho informado inválido"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEvento(@PathVariable String id) {
        try {
            Evento existente = eventoService.findById(Long.parseLong(id));
            if (!existente.getIdUsuarioFk().equals(getUsuarioAutenticado().getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));
            }
            eventoService.deleteById(Long.parseLong(id));
            return ResponseEntity.ok(Map.of("status", 200, "retorno", "OK", "message", "Evento deletado com o ID: " + id));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "O id informado não é válido: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage()));
        }
    }
}
