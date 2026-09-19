package br.com.reserva;

public class Sala {
    private String numero;
    private int capacidade;
    private boolean emManutencao;

    public Sala(String numero, int capacidade, boolean emManutencao) {
        this.numero = numero;
        this.capacidade = capacidade;
        this.emManutencao = emManutencao;
    }

    public String getNumero() { return numero; }
    public int getCapacidade() { return capacidade; }
    public boolean isEmManutencao() { return emManutencao; }
}