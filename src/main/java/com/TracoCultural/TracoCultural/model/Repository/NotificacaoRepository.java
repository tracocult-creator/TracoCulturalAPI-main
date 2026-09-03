package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByIdUsuarioFkOrderByDataCriacaoDesc(Long usuarioId);
    long countByIdUsuarioFkAndLidaFalse(Long usuarioId);
    boolean existsByIdUsuarioFkAndIdEventoFkAndTipo(Long usuarioId, Long eventoId, String tipo);
    // Limpeza ao excluir usuário/evento -- sem FK real no banco, mas evita
    // notificação órfã apontando pra quem/o que não existe mais.
    void deleteByIdUsuarioFk(Long idUsuarioFk);
    void deleteByIdEventoFk(Long idEventoFk);
}