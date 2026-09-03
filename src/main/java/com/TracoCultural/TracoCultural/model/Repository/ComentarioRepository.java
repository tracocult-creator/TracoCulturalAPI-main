package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByIdEventoFkOrderByDataCriacaoDesc(Long idEventoFk);

    void deleteByIdEventoFk(Long idEventoFk);

    // Comentários feitos POR esse usuário em eventos de OUTRAS pessoas --
    // apagar só os eventos dele não cobre isso. Sem FK real no banco (não
    // quebra a exclusão), mas fica lixo órfão se não limpar.
    void deleteByIdUsuarioFk(Long idUsuarioFk);
}