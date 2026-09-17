package com.TracoCultural.TracoCultural.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Dispara sempre que uma rota protegida é acessada sem um token válido
 * (ausente, expirado ou malformado). Sem esse componente, o Spring Security
 * usa o comportamento padrão e devolve 403 -- que o front não distingue de
 * "usuário autenticado mas sem permissão", então nunca dispara o logout
 * automático (que só acontece no interceptor do axios para status 401).
 *
 * Com isso aqui: token ausente/inválido = 401 (sessão precisa ser refeita).
 * Usuário autenticado tentando editar/excluir algo que não é dele = 403
 * (continua sendo tratado manualmente nos controllers, ex: EventoController).
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "status", 401,
                "retorno", "Unauthorized",
                "message", "Sessão inválida ou expirada. Faça login novamente."
        )));
    }
}