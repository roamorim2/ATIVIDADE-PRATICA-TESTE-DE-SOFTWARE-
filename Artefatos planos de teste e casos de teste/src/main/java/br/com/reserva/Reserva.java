package br.com.reserva;

import java.time.LocalTime;

public class Reserva {
    private Sala sala;
    private Turma turma;
    private Usuario responsavel;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private boolean cancelada = false;

    public Reserva(Sala sala, Turma turma, Usuario responsavel, LocalTime horaInicio, LocalTime horaFim) {
        this.sala = sala;
        this.turma = turma;
        this.responsavel = responsavel;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Sala getSala() { return sala; }
    public Turma getTurma() { return turma; }
    public Usuario getResponsavel() { return responsavel; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public boolean isCancelada() { return cancelada; }
    public void setCancelada(boolean cancelada) { this.cancelada = cancelada; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
}