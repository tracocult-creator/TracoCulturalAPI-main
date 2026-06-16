package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
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
    public ResponseEntity<List<Usuario>> ListarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> listarProdutoPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(usuarioServices.findById(Long.parseLong(id)));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "O id informado não é valido: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuario não encontrado com o ID: " + id));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Object> SalvarUsuario(@RequestBody Usuario usuario) {
        Usuario novo = usuarioServices.save(usuario);
        String token = jwtUtil.gerarToken(novo.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("token", token, "id", novo.getId(), "nome", novo.getNome(),
                        "email", novo.getEmail(), "isAdm", novo.getIsAdm()));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> logar(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        Usuario usuarioDoBanco = usuarioRepository.findByEmail(email);
        if (usuarioDoBanco == null || !usuarioDoBanco.getSenha().equals(senha)) {
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "retorno", "Unauthorized", "message", "Email ou senha inválidos"));
        }
        return ResponseEntity.ok(usuarioDoBanco);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> DeletarUsuario(@PathVariable String id) {
        String emailAutenticado = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario autenticado = usuarioRepository.findByEmail(emailAutenticado);
        try {
            if (!autenticado.getId().equals(Long.parseLong(id))) {
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho inválido"));
        }
        return usuarioServices.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> AtualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        String emailAutenticado = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario autenticado = usuarioRepository.findByEmail(emailAutenticado);
        try {
            if (!autenticado.getId().equals(Long.parseLong(id))) {
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado"));
            }
            return ResponseEntity.ok(usuarioServices.update(Long.parseLong(id), usuario));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho informado inválido"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuario não encontrado com o ID: " + id));
        }
    }
}
