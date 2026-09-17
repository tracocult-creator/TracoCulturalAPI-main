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
    List<Evento> findByIdUsuarioFk(Long idUsuarioFk);
    void deleteByIdUsuarioFk(Long idUsuarioFk);

    // Variantes usadas nas listagens públicas (Home, mapa, busca) -- só
    // trazem eventos já aprovados por um admin. "Meus eventos" e a listagem
    // do painel admin continuam usando os métodos sem esse filtro, de propósito.
    List<Evento> findByAprovadoTrue();
    List<Evento> findByCidadeIgnoreCaseAndAprovadoTrue(String cidade);
    List<Evento> findByCategoriaIdAndAprovadoTrue(Long categoriaId);
    List<Evento> findByCidadeIgnoreCaseAndCategoriaIdAndAprovadoTrue(String cidade, Long categoriaId);
}
