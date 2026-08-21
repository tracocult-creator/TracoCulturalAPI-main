package com.TracoCultural.TracoCultural.model.Repository;

import com.TracoCultural.TracoCultural.model.entity.Compartilhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompartilhamentoRepository extends JpaRepository<Compartilhamento, Long> {
    long countByEvento_Id(Long eventoId);
}
