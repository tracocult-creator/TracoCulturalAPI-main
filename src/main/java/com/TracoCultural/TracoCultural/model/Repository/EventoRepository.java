package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByCidadeIgnoreCase(String cidade);
    List<Evento> findByCategoriaId(Long categoriaId);
    List<Evento> findByCidadeIgnoreCaseAndCategoriaId(String cidade, Long categoriaId);
}
