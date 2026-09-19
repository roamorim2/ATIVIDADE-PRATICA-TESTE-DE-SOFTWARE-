package br.com.reserva;

import java.time.LocalTime;

public class ReservaService {

    private final NotificacaoService notificacaoService;
    private final AuditoriaService auditoriaService;
    private final ReservaRepository reservaRepository;

    public ReservaService(NotificacaoService notificacaoService, AuditoriaService auditoriaService, ReservaRepository reservaRepository) {
        this.notificacaoService = notificacaoService;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = reservaRepository;
    }

    public Reserva reservarSala(Sala sala, Turma turma, Usuario usuario, LocalTime inicio, LocalTime fim) {
        if (sala.isEmManutencao()) {
            throw new RegraNegocioException("Sala indisponível devido a manutenção.");
        }
        if (turma.getTamanho() > sala.getCapacidade()) {
            throw new RegraNegocioException("Capacidade da sala é inferior ao tamanho da turma.");
        }
        if (inicio.isBefore(LocalTime.of(7, 30)) || fim.isAfter(LocalTime.of(22, 30))) {
            throw new RegraNegocioException("Horário de reserva deve ser entre 07h30 e 22h30.");
        }
        if (reservaRepository.existeSobreposicao(sala, inicio, fim)) {
            throw new RegraNegocioException("A sala já possui reserva no horário selecionado.");
        }

        Reserva reserva = new Reserva(sala, turma, usuario, inicio, fim);
        auditoriaService.registrarAcao("Reserva criada para a sala " + sala.getNumero());
        return reserva;
    }

    public void alterarReserva(Reserva reserva, Usuario solicitante, Sala novaSala, LocalTime novoInicio, LocalTime novoFim) {
        if (!reserva.getResponsavel().equals(solicitante) && solicitante.getPerfil() != Perfil.COORDENACAO) {
            throw new AcessoNegadoException("Apenas a coordenação pode alterar reservas de outros professores.");
        }

        reserva.setHoraInicio(novoInicio);
        reserva.setHoraFim(novoFim);
        notificacaoService.enviarNotificacao(reserva.getResponsavel(), "Sua reserva foi alterada.");
        auditoriaService.registrarAcao("Reserva alterada pelo usuário " + solicitante.getNome());
    }

    public void cancelarReserva(Reserva reserva, Usuario solicitante) {
        reserva.setCancelada(true);
        notificacaoService.enviarNotificacao(reserva.getResponsavel(), "Sua reserva foi cancelada.");
        auditoriaService.registrarAcao("Reserva cancelada pelo usuário " + solicitante.getNome());
    }
}