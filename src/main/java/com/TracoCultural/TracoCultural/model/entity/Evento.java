package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "Evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "cardImage")
    private byte[] cardImage;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "dataInicio", nullable = false)
    private Date dataInicio;

    @Column(name = "dataFim")
    private Date dataFim;

    @NotBlank(message = "Cidade é obrigatória")
    @Column(length = 45)
    private String cidade;

    @Column(name = "linkExterno", length = 255)
    private String linkExterno;

    @Column(name = "idUsuarioFk")
    private Long idUsuarioFk;

    @NotNull(message = "Categoria é obrigatória")
    @ManyToOne
    @JoinColumn(name = "idCategoriaFk")
    private Categoria categoria;

    @Column(name = "destacado")
    private Boolean destacado = false;

    @Column(name = "patrocinado")
    private Boolean patrocinado = false;



    //              -------------------------------- Getter e Setter --------------------------------


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public byte[] getCardImage() {
        return cardImage;
    }

    public void setCardImage(byte[] cardImage) {
        this.cardImage = cardImage;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getLinkExterno() {
        return linkExterno;
    }

    public void setLinkExterno(String linkExterno) {
        this.linkExterno = linkExterno;
    }

    public Long getIdUsuarioFk() {
        return idUsuarioFk;
    }

    public void setIdUsuarioFk(Long idUsuarioFk) {
        this.idUsuarioFk = idUsuarioFk;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Boolean getDestacado() { return destacado; }
    public void setDestacado(Boolean destacado) { this.destacado = destacado; }

    public Boolean getPatrocinado() { return patrocinado; }
    public void setPatrocinado(Boolean patrocinado) { this.patrocinado = patrocinado; }
}
