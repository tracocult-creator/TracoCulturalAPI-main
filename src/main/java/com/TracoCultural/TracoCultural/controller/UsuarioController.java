package com.TracoCultural.TracoCultural.controller;


import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.UsuarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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



    //GET
    @GetMapping
    public ResponseEntity<List<Usuario>> ListarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }


    //GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> listarProdutoPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(usuarioServices.findById(Long.parseLong((id))));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "retorno", "Bad Request",
                            "message", "O id informado não é valido: " + id
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "status", 404,
                            "retorno", "Not Found",
                            "message", "Usuario não encontrado com o ID: " + id
                    )
            );
        }
    }


    //POST
    @PostMapping("/auth/register")
    public ResponseEntity<Usuario> SalvarUsuario(@RequestBody Usuario usuario) {
        Usuario novo = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }


    // POST LOGIN
    @PostMapping("/login")
    public ResponseEntity<Object> logar(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        Usuario usuarioDoBanco = usuarioRepository.findByEmail(email);
        if (usuarioDoBanco == null || !usuarioDoBanco.getSenha().equals(senha)) {
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "retorno", "Unauthorized", "message", "Email ou senha inválidos")
            );
        }
        return ResponseEntity.ok(usuarioDoBanco);
    }


    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> DeletarUsuario(@PathVariable String id) {
            return usuarioServices.deleteById(id);
    }


    //ATUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<Object> AtualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        try{
            return ResponseEntity.ok(usuarioServices.update(Long.parseLong(id), usuario));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "retorno", "Bad Request",
                            "message", "Caminho informado inválido"
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "status", 404,
                            "retorno", "Not Found",
                            "message", "Usuario não encontrado com o ID: " + id
                    ));
        }
    }



}





//  @RequestBody  : Corpo da Requisição ( Recebendo um objeto JSON )
//  ResponseEntity: Toda resposta HTTP (status, cabeçalhos e corpo), aqui temos mais controle sobre
//  o que é devolvido para o cliente

//  1. Status HTTP: (200 ok,201 CREATED, 404 NOT FOUND etc...)
//  2. Headers: ( cabeçalhos extras, como location, Authorization etc...)
//  3. Body: ( o objeto que será convertido em JSON/XML para o cliente )




