package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Representa UM envio de notificação em lote — seja o admin mandando um
 * aviso geral pra todo mundo, seja o dono de um evento avisando quem
 * favoritou. Cada linha aqui agrupa várias linhas de {@link Notificacao}
 * (uma por destinatário), permitindo editar ou excluir o envio inteiro
 * de uma vez, em vez de mexer notificação por notificação.
 */
@Entity
@Table(name = "EnvioNotificacao")
public class EnvioNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario_remetente_fk", nullable = false)
    private Long idUsuarioRemetenteFk;

    // null quando é um envio geral (admin -> todo mundo)
    @Column(name = "id_evento_fk")
    private Long idEventoFk;

    // "GERAL" (admin) ou "EVENTO_ATUALIZACAO" (dono do evento -> favoritos)
    @Column(length = 30, nullable = false)
    private String tipo;

    @Column(length = 255, nullable = false)
    private String mensagem;

    @Column(name = "total_destinatarios", nullable = false)
    private int totalDestinatarios;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuarioRemetenteFk() { return idUsuarioRemetenteFk; }
    public void setIdUsuarioRemetenteFk(Long idUsuarioRemetenteFk) { this.idUsuarioRemetenteFk = idUsuarioRemetenteFk; }

    public Long getIdEventoFk() { return idEventoFk; }
    public void setIdEventoFk(Long idEventoFk) { this.idEventoFk = idEventoFk; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public int getTotalDestinatarios() { return totalDestinatarios; }
    public void setTotalDestinatarios(int totalDestinatarios) { this.totalDestinatarios = totalDestinatarios; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}