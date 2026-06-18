package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.dto.UsuarioDTO;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.UsuarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioServices usuarioServices;
    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> lista = usuarioRepository.findAll().stream()
                .map(UsuarioDTO::new).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> listarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(new UsuarioDTO(usuarioServices.findById(Long.parseLong(id))));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "O id informado não é válido: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado com o ID: " + id));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Object> salvarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario novo = usuarioServices.save(usuario);
            String token = jwtUtil.gerarToken(novo.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("token", token, "usuario", new UsuarioDTO(novo)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict", "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> logar() {
        return ResponseEntity.status(410).body(
                Map.of("status", 410, "retorno", "Gone", "message", "Este endpoint foi desativado. Use POST /api/v1/auth/login"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        String emailAutenticado = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario autenticado = usuarioRepository.findByEmail(emailAutenticado);
        try {
            if (!autenticado.getId().equals(Long.parseLong(id)))
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));

            return ResponseEntity.ok(new UsuarioDTO(usuarioServices.update(Long.parseLong(id), usuario)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict", "message", e.getMessage()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho informado inválido"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado com o ID: " + id));
        }
    }

    @PatchMapping("/{id}/senha")
    public ResponseEntity<Object> atualizarSenha(@PathVariable String id,
                                                  @RequestBody Map<String, String> body) {
        String emailAutenticado = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario autenticado = usuarioRepository.findByEmail(emailAutenticado);
        try {
            if (!autenticado.getId().equals(Long.parseLong(id)))
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));

            usuarioServices.atualizarSenha(Long.parseLong(id),
                    body.get("senhaAtual"), body.get("novaSenha"));
            return ResponseEntity.ok(Map.of("status", 200, "retorno", "OK", "message", "Senha atualizada com sucesso"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho informado inválido"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado com o ID: " + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarUsuario(@PathVariable String id) {
        String emailAutenticado = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario autenticado = usuarioRepository.findByEmail(emailAutenticado);
        try {
            if (!autenticado.getId().equals(Long.parseLong(id)))
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho inválido"));
        }
        return usuarioServices.deleteById(id);
    }
}
