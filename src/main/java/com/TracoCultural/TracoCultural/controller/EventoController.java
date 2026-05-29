package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.services.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // GET /api/v1/eventos
    // GET /api/v1/eventos?cidade=SP
    // GET /api/v1/eventos?categoriaId=1
    // GET /api/v1/eventos?cidade=SP&categoriaId=1
    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Long categoriaId) {

        if (cidade != null && categoriaId != null)
            return ResponseEntity.ok(eventoService.findByCidadeAndCategoria(cidade, categoriaId));
        if (cidade != null)
            return ResponseEntity.ok(eventoService.findByCidade(cidade));
        if (categoriaId != null)
            return ResponseEntity.ok(eventoService.findByCategoria(categoriaId));

        return ResponseEntity.ok(eventoService.findAll());
    }

    // GET /api/v1/eventos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(eventoService.findById(Long.parseLong(id)));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "O id informado não é válido: " + id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage())
            );
        }
    }

    // POST /api/v1/eventos
    @PostMapping
    public ResponseEntity<Evento> publicarEvento(@RequestBody Evento evento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.save(evento));
    }

    // PUT /api/v1/eventos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarEvento(@PathVariable String id, @RequestBody Evento evento) {
        try {
            return ResponseEntity.ok(eventoService.update(Long.parseLong(id), evento));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "Caminho informado inválido")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage())
            );
        }
    }

    // DELETE /api/v1/eventos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEvento(@PathVariable String id) {
        try {
            eventoService.deleteById(Long.parseLong(id));
            return ResponseEntity.ok(
                    Map.of("status", 200, "retorno", "OK",
                           "message", "Evento deletado com o ID: " + id)
            );
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "O id informado não é válido: " + id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage())
            );
        }
    }
}