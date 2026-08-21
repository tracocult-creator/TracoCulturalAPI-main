package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Notificacao;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName());
        if (usuario == null) {
            throw new RuntimeException("Usuário autenticado não encontrado");
        }
        return usuario;
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listar(Authentication auth) {
        Usuario usuario = usuarioAutenticado(auth);
        return ResponseEntity.ok(notificacaoService.listar(usuario.getId()));
    }

    @GetMapping("/nao-lidas/contagem")
    public ResponseEntity<Object> contarNaoLidas(Authentication auth) {
        Usuario usuario = usuarioAutenticado(auth);
        return ResponseEntity.ok(Map.of("total", notificacaoService.contarNaoLidas(usuario.getId())));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Object> marcarComoLida(@PathVariable Long id, Authentication auth) {
        Usuario usuario = usuarioAutenticado(auth);
        try {
            notificacaoService.marcarComoLida(id, usuario.getId());
            return ResponseEntity.ok(Map.of("status", 200, "message", "Notificação marcada como lida"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("status", 403, "message", "Sem permissão"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Object> marcarTodasComoLidas(Authentication auth) {
        Usuario usuario = usuarioAutenticado(auth);
        notificacaoService.marcarTodasComoLidas(usuario.getId());
        return ResponseEntity.ok(Map.of("status", 200, "message", "Todas as notificações foram marcadas como lidas"));
    }
}
