package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.services.CompartilhamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/compartilhamentos")
public class CompartilhamentoController {

    @Autowired
    private CompartilhamentoService compartilhamentoService;

    // Registrado só quando o usuário está logado (compartilhar não exige
    // login no front — se não estiver logado, o front só abre o link/share
    // nativo sem chamar esse endpoint).
    @PostMapping
    public ResponseEntity<Object> registrar(@PathVariable Long eventoId, Authentication auth) {
        try {
            long total = compartilhamentoService.registrar(eventoId, auth.getName());
            return ResponseEntity.status(201).body(Map.of("total", total));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("status", 404, "message", e.getMessage()));
        }
    }

    @GetMapping("/contagem")
    public ResponseEntity<Object> contar(@PathVariable Long eventoId) {
        return ResponseEntity.ok(Map.of("total", compartilhamentoService.contar(eventoId)));
    }
}
