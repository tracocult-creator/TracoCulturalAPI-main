package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Destinatário da notificação
    @Column(name = "id_usuario_fk", nullable = false)
    private Long idUsuarioFk;

    // Evento relacionado (opcional — nem toda notificação precisa de um)
    @Column(name = "id_evento_fk")
    private Long idEventoFk;

    @Column(length = 255, nullable = false)
    private String mensagem;

    // "COMENTARIO" | "EVENTO_PROXIMO"
    @Column(length = 30, nullable = false)
    private String tipo;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuarioFk() { return idUsuarioFk; }
    public void setIdUsuarioFk(Long idUsuarioFk) { this.idUsuarioFk = idUsuarioFk; }

    public Long getIdEventoFk() { return idEventoFk; }
    public void setIdEventoFk(Long idEventoFk) { this.idEventoFk = idEventoFk; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
