package br.com.reserva;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String message) { super(message); }
}