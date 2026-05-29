package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Usado no AuthController (login) e FavoritoController (token JWT → usuário)
    Usuario findByEmail(String email);

    // Verificação rápida de email duplicado antes de salvar
    boolean existsByEmail(String email);
}