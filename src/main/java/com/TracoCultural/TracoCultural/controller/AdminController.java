package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.AdminService;
import com.TracoCultural.TracoCultural.model.services.EventoService;
import com.TracoCultural.TracoCultural.model.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private EventoService eventoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private NotificacaoService notificacaoService;

    private ResponseEntity<Object> forbidden() {
        return ResponseEntity.status(403).body(
            Map.of("status", 403, "message", "Acesso restrito a administradores")
        );
    }

    private Usuario autenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName());
    }

//dashborrd

    @GetMapping("/dashboard")
    public ResponseEntity<Object> dashboard(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        return ResponseEntity.ok(adminService.dashboard());
    }

    //usuarios

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

    //eventos

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

    @PatchMapping("/eventos/{id}/aprovar")
    public ResponseEntity<Object> aprovarEvento(@PathVariable Long id, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        try {
            return ResponseEntity.ok(adminService.aprovarEvento(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @PostMapping("/eventos/limpar-encerrados")
    public ResponseEntity<Object> limparEventosEncerrados(Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();
        int removidos = eventoService.limparEventosEncerrados();
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", removidos == 0
                        ? "Nenhum evento encerrado há mais de 3 dias encontrado."
                        : removidos + " evento(s) removido(s).",
                "removidos", removidos
        ));
    }

    //comentrios

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

    //notificacoes

    @PostMapping("/notificacoes")
    public ResponseEntity<Object> enviarNotificacao(@RequestBody Map<String, String> body, Authentication auth) {
        if (!autenticado(auth).getIsAdm()) return forbidden();

        String mensagem = body.get("mensagem") != null ? body.get("mensagem").trim() : null;
        if (mensagem == null || mensagem.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", 400, "message", "Mensagem é obrigatória"));
        }

        var envio = notificacaoService.enviarGeral(mensagem, autenticado(auth).getId());
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Notificação enviada",
                "totalEnviado", envio.getTotalDestinatarios(),
                "envio", envio
        ));
    }
}