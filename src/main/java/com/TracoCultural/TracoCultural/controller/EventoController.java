package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EventoService;
import com.TracoCultural.TracoCultural.model.services.NotificacaoService;
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
    private EventoRepository eventoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private NotificacaoService notificacaoService;

    private Usuario getUsuarioAutenticado() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioRepository.findByEmail(email);
    }

    @GetMapping
    public ResponseEntity<Object> listarEventos(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (idUsuario != null)
            return ResponseEntity.ok(eventoRepository.findByIdUsuarioFk(idUsuario));

        // Busca textual e/ou paginação: só entra nesse caminho se o cliente
        // pedir explicitamente (q e/ou page/size), pra não quebrar quem
        // consome a lista simples (ex: app mobile).
        if (q != null || page != null || size != null) {
            int pageFinal = page != null ? Math.max(page, 0) : 0;
            int sizeFinal = size != null && size > 0 ? size : 12;
            return ResponseEntity.ok(eventoService.buscarPaginado(q, categoriaId, cidade, pageFinal, sizeFinal));
        }

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
    public ResponseEntity<Object> atualizarEvento(@PathVariable String id, @RequestBody Evento evento) {
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

    // Só o dono do evento (ou um admin) pode notificar quem favoritou ESSE
    // evento -- diferente do /admin/notificacoes, que é geral pra todo mundo.
    @PostMapping("/{id}/notificar-favoritos")
    public ResponseEntity<Object> notificarFavoritos(@PathVariable String id, @RequestBody Map<String, String> body) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("status", 401, "message", "Não autenticado"));
        }

        Evento evento;
        try {
            evento = eventoService.findById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "message", "O id informado não é válido: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }

        boolean ehDono = evento.getIdUsuarioFk() != null && evento.getIdUsuarioFk().equals(usuario.getId());
        if (!ehDono && !usuario.getIsAdm()) {
            return ResponseEntity.status(403).body(
                    Map.of("status", 403, "message", "Só o criador do evento pode notificar quem favoritou"));
        }

        String mensagem = body.get("mensagem") != null ? body.get("mensagem").trim() : null;
        if (mensagem == null || mensagem.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", 400, "message", "Mensagem é obrigatória"));
        }

        int total = notificacaoService.notificarFavoritosDoEvento(evento.getId(), mensagem);
        return ResponseEntity.ok(Map.of("status", 200, "message", "Notificação enviada", "totalEnviado", total));
    }
}