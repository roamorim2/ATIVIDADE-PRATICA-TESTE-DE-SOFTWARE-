package br.com.reserva;

import java.time.LocalTime;

public interface ReservaRepository {
    boolean existeSobreposicao(Sala sala, LocalTime inicio, LocalTime fim);
}