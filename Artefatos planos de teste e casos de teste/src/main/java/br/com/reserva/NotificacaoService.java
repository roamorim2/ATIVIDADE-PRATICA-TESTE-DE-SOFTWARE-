package br.com.reserva;

public interface NotificacaoService {
    void enviarNotificacao(Usuario destinatario, String mensagem);
}