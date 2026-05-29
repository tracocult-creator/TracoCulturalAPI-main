package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 45, nullable = false, unique = true)   // ← unique adicionado
    private String email;

    @Column(length = 255, nullable = false)
    private String senha;

    @Lob
@Column(name = "fotoPerfil", nullable = true)
private byte[] fotoPerfil;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean isAdm;


    // -------------------------------- Getters e Setters --------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public byte[] getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(byte[] fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public boolean getIsAdm() { return isAdm; }
    public void setIsAdm(boolean isAdm) { this.isAdm = isAdm; }
}