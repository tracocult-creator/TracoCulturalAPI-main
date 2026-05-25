package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Favorito;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private UsuarioRepository usuarioRepository;


    // Retorna o usuário autenticado a partir do email no token JWT
    private Usuario usuarioAutenticado() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuário autenticado não encontrado");
        }
        return usuario;
    }


    // GET /api/v1/favoritos
    @GetMapping
    public ResponseEntity<List<Favorito>> listarFavoritos() {
        Usuario usuario = usuarioAutenticado();
        return ResponseEntity.ok(favoritoService.listarPorUsuario(usuario.getId()));
    }


    // POST /api/v1/favoritos
    // Body: { "idEventoFk": 1 }  (idUsuarioFk é ignorado — vem do token)
    @PostMapping
    public ResponseEntity<Object> favoritar(@RequestBody Map<String, Long> body) {
        Long eventoId = body.get("idEventoFk");
        if (eventoId == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "idEventoFk é obrigatório")
            );
        }
        try {
            Usuario usuario = usuarioAutenticado();
            Favorito favorito = favoritoService.favoritar(usuario.getId(), eventoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
        } catch (RuntimeException e) {
            int status = e.getMessage().contains("já está") ? 409 : 404;
            return ResponseEntity.status(status).body(
                    Map.of("status", status, "message", e.getMessage())
            );
        }
    }


    // DELETE /api/v1/favoritos/{eventoId}
    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Object> desfavoritar(@PathVariable Long eventoId) {
        try {
            Usuario usuario = usuarioAutenticado();
            favoritoService.desfavoritar(usuario.getId(), eventoId);
            return ResponseEntity.ok(
                    Map.of("status", 200, "retorno", "OK", "message", "Favorito removido")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", e.getMessage())
            );
        }
    }
}
