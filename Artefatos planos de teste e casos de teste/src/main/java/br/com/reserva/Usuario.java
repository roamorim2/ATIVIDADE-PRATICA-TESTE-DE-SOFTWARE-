package br.com.reserva;

public class Usuario {
    private String nome;
    private Perfil perfil;

    public Usuario(String nome, Perfil perfil) {
        this.nome = nome;
        this.perfil = perfil;
    }

    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }
}