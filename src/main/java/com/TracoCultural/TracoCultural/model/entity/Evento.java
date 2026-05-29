package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Lob                                                    // ← mapeia VARBINARY(MAX)
    @Column(nullable = true)
    private byte[] cardImage;

    @Column(nullable = false)
    private Date dataInicio;

    private Date dataFim;

    @Column(length = 45)
    private String cidade;

    @Column(length = 255)
    private String linkExterno;

    // ← Era um Long solto (órfão ao deletar usuário). Agora é FK real.
    @ManyToOne
    @JoinColumn(name = "idUsuarioFk", nullable = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idCategoriaFk")
    private Categoria categoria;


    // -------------------------------- Getters e Setters --------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public byte[] getCardImage() { return cardImage; }
    public void setCardImage(byte[] cardImage) { this.cardImage = cardImage; }

    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }

    public Date getDataFim() { return dataFim; }
    public void setDataFim(Date dataFim) { this.dataFim = dataFim; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getLinkExterno() { return linkExterno; }
    public void setLinkExterno(String linkExterno) { this.linkExterno = linkExterno; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}