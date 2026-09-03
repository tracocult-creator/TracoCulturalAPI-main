package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByUsuarioId(Long usuarioId);
    List<Favorito> findByEventoId(Long eventoId);
    Optional<Favorito> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);
    boolean existsByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);
    void deleteByEventoId(Long eventoId);
    // Precisa existir ANTES de apagar um usuário -- Favorito.usuario tem FK
    // NOT NULL no banco, então apagar o usuário sem isso quebra com erro
    // de violação de chave estrangeira.
    void deleteByUsuarioId(Long usuarioId);
}