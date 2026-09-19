package br.com.reserva;

public class Turma {
    private String nome;
    private int tamanho;

    public Turma(String nome, int tamanho) {
        this.nome = nome;
        this.tamanho = tamanho;
    }

    public String getNome() { return nome; }
    public int getTamanho() { return tamanho; }
}