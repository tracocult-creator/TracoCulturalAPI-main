package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado com o ID: " + id));
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario save(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario update(Long id, Usuario usuario) {
        Usuario existente = findById(id);
        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        // Só re-encripta se uma nova senha foi enviada
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        if (usuario.getEstado() != null) existente.setEstado(usuario.getEstado());
        if (usuario.getIcone() != null) existente.setIcone(usuario.getIcone());
        if (usuario.getCorFundo() != null) existente.setCorFundo(usuario.getCorFundo());
        return usuarioRepository.save(existente);
    }

    public boolean idExists(String id) {
        return usuarioRepository.existsById(Long.parseLong(id));
    }

    public ResponseEntity<Object> deleteById(String id) {
        try {
            if (idExists(id)) {
                usuarioRepository.deleteById(Long.parseLong(id));
                return ResponseEntity.ok(
                        Map.of("status", 200, "retorno", "OK", "message", "Usuario deletado com o ID: " + id)
                );
            }
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuario não encontrado com o ID: " + id)
            );
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Caminho inválido")
            );
        }
    }
}
