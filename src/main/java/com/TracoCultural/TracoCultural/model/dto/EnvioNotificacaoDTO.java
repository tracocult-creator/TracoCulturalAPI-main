package com.TracoCultural.TracoCultural.model.dto;

import com.TracoCultural.TracoCultural.model.entity.EnvioNotificacao;

import java.time.LocalDateTime;

public class EnvioNotificacaoDTO {

    private Long id;
    private String tipo;
    private String mensagem;
    private Long idEventoFk;
    private String nomeEvento; // null pra envios gerais do admin
    private int totalDestinatarios;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public EnvioNotificacaoDTO(EnvioNotificacao envio, String nomeEvento) {
        this.id = envio.getId();
        this.tipo = envio.getTipo();
        this.mensagem = envio.getMensagem();
        this.idEventoFk = envio.getIdEventoFk();
        this.nomeEvento = nomeEvento;
        this.totalDestinatarios = envio.getTotalDestinatarios();
        this.dataCriacao = envio.getDataCriacao();
        this.dataAtualizacao = envio.getDataAtualizacao();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getMensagem() { return mensagem; }
    public Long getIdEventoFk() { return idEventoFk; }
    public String getNomeEvento() { return nomeEvento; }
    public int getTotalDestinatarios() { return totalDestinatarios; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}