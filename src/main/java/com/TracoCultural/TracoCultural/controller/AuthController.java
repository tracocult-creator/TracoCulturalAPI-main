package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getSenha() == null || usuario.getNome() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Nome, email e senha são obrigatórios")
            );
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict", "message", "Email já cadastrado")
            );
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario novo = usuarioRepository.save(usuario);
        String token = jwtUtil.gerarToken(novo.getEmail());

        return ResponseEntity.status(201).body(Map.of(
                "token", token,
                "id",    novo.getId(),
                "nome",  novo.getNome(),
                "email", novo.getEmail(),
                "isAdm", novo.getIsAdm()
        ));
    }

    // POST /api/v1/auth/login
    // Body: { "email": "...", "senha": "..." }
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email e senha são obrigatórios")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenha())) {
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "retorno", "Unauthorized", "message", "Email ou senha inválidos")
            );
        }

        String token = jwtUtil.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(Map.of(
                "token",  token,
                "id",     usuario.getId(),
                "nome",   usuario.getNome(),
                "email",  usuario.getEmail(),
                "isAdm",  usuario.getIsAdm()
        ));
    }
}
