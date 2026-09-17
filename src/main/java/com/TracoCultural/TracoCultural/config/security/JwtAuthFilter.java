package com.TracoCultural.TracoCultural.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String rota = request.getMethod() + " " + request.getRequestURI();

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtUtil.validarToken(token)) {
                    String email = jwtUtil.extrairEmail(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());

                    // Cria um SecurityContext novo e seta explicitamente, em vez de
                    // mutar o contexto existente via getContext().setAuthentication().
                    // No Spring Security 6, o SecurityContextHolderFilter já roda antes
                    // desse filtro e gerencia o ciclo de vida do contexto -- criar um
                    // contexto novo aqui garante que a autenticação realmente "gruda"
                    // pro resto da cadeia (incluindo o AuthorizationFilter, que decide
                    // se a rota é liberada ou não).
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);
                } else {
                    System.err.println("[JWT DEBUG] Token presente mas inválido em " + rota);
                }
            } catch (Exception e) {
                System.err.println("[JWT DEBUG] Erro inesperado processando token em " + rota + ": "
                        + e.getClass().getSimpleName() + " -> " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.err.println("[JWT DEBUG] Sem header Authorization Bearer em " + rota);
        }

        filterChain.doFilter(request, response);
    }
}
