package com.TracoCultural.TracoCultural.model.Dto;

import com.TracoCultural.TracoCultural.model.entity.Usuario;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean isAdm;

    public UsuarioDTO() {}

    public UsuarioDTO(Usuario usuario) {
        this.id    = usuario.getId();
        this.nome  = usuario.getNome();
        this.email = usuario.getEmail();
        this.isAdm = usuario.getIsAdm();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean getIsAdm() { return isAdm; }
    public void setIsAdm(boolean isAdm) { this.isAdm = isAdm; }
}