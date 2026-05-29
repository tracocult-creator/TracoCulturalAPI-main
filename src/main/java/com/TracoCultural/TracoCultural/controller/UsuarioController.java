package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Dto.UsuarioDTO;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.UsuarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioServices usuarioServices;


    // GET /api/v1/usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> lista = usuarioRepository.findAll()
                .stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }


    // GET /api/v1/usuarios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> listarPorId(@PathVariable String id) {
        try {
            Usuario usuario = usuarioServices.findById(Long.parseLong(id));
            return ResponseEntity.ok(new UsuarioDTO(usuario));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "O id informado não é válido: " + id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found",
                           "message", "Usuário não encontrado com o ID: " + id)
            );
        }
    }


    // POST /api/v1/usuarios/auth/register
    @PostMapping("/auth/register")
    public ResponseEntity<Object> salvarUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict",
                           "message", "E-mail já cadastrado: " + usuario.getEmail())
            );
        }
        try {
            Usuario novo = usuarioServices.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDTO(novo));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict",
                           "message", "E-mail já cadastrado: " + usuario.getEmail())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("status", 500, "retorno", "Internal Server Error",
                           "message", "Erro ao cadastrar usuário: " + e.getMessage())
            );
        }
    }


    // DELETE /api/v1/usuarios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarUsuario(@PathVariable String id) {
        return usuarioServices.deleteById(id);
    }


    // PUT /api/v1/usuarios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        try {
            Usuario atualizado = usuarioServices.update(Long.parseLong(id), usuario);
            return ResponseEntity.ok(new UsuarioDTO(atualizado));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "Caminho informado inválido")
            );
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict",
                           "message", "E-mail já está em uso por outro usuário")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found",
                           "message", "Usuário não encontrado com o ID: " + id)
            );
        }
    }
}