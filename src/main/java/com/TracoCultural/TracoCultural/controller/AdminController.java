package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private UsuarioRepository usuarioRepository;

    private ResponseEntity<Object> forbidden() {
        return ResponseEntity.status(403).body(
            Map.of("status", 403, "message", "Acesso restrito a administradores")
        );
    }

    private Usuario autenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName());
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<Object> dashboard(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        return ResponseEntity.ok(adminService.dashboard());
    }

    // ── Usuários ───────────────────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public ResponseEntity<Object> listarUsuarios(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Object> deletarUsuario(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            adminService.deletarUsuario(id);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Usuário excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @PatchMapping("/usuarios/{id}/admin")
    public ResponseEntity<Object> alternarAdmin(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            return ResponseEntity.ok(adminService.alternarAdmin(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    // ── Eventos ────────────────────────────────────────────────────────────────

    @GetMapping("/eventos")
    public ResponseEntity<Object> listarEventos(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        return ResponseEntity.ok(adminService.listarEventos());
    }

    @PutMapping("/eventos/{id}")
    public ResponseEntity<Object> editarEvento(@PathVariable Long id,
                                               @RequestBody Evento dados,
                                               Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            return ResponseEntity.ok(adminService.editarEvento(id, dados));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/eventos/{id}")
    public ResponseEntity<Object> deletarEvento(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            adminService.deletarEvento(id);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Evento excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @PatchMapping("/eventos/{id}/destacar")
    public ResponseEntity<Object> destacarEvento(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            return ResponseEntity.ok(adminService.destacarEvento(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @PatchMapping("/eventos/{id}/patrocinar")
    public ResponseEntity<Object> patrocinarEvento(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            return ResponseEntity.ok(adminService.patrocinarEvento(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    // ── Comentários ────────────────────────────────────────────────────────────

    @GetMapping("/comentarios")
    public ResponseEntity<Object> listarComentarios(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        return ResponseEntity.ok(adminService.listarComentarios());
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Object> deletarComentario(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            adminService.deletarComentario(id);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Comentário excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }
}
