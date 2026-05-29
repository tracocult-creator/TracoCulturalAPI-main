package com.TracoCultural.TracoCultural.config;

import com.TracoCultural.TracoCultural.config.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // ← Desabilita o form login e http basic padrão do Spring
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/auth/register").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/v1/eventos").permitAll()  // ← temporário para testar
    .requestMatchers(HttpMethod.GET,  "/api/v1/eventos").permitAll()
    .requestMatchers(HttpMethod.GET,  "/api/v1/eventos/**").permitAll()
    .anyRequest().authenticated()
)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
            "http://localhost:5176", "http://localhost:5177", "http://localhost:5178",
            "http://localhost:5179", "http://localhost:8686",
            "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175",
            "http://127.0.0.1:5176", "http://127.0.0.1:5177", "http://127.0.0.1:5178",
            "http://127.0.0.1:5179", "http://127.0.0.1:8686"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}