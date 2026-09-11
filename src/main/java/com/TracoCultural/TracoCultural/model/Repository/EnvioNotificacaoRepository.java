package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.EnvioNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioNotificacaoRepository extends JpaRepository<EnvioNotificacao, Long> {
    List<EnvioNotificacao> findByIdUsuarioRemetenteFkOrderByDataCriacaoDesc(Long idUsuarioRemetenteFk);
    List<EnvioNotificacao> findByIdEventoFkOrderByDataCriacaoDesc(Long idEventoFk);
    List<EnvioNotificacao> findByTipoOrderByDataCriacaoDesc(String tipo);
    void deleteByIdEventoFk(Long idEventoFk);
}