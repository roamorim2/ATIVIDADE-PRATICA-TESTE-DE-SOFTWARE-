package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacaoTest {

    @Test
    void deveCalcularPontosComAmbosVerdadeiros() {
        Participacao participacao = new Participacao();
        // entregouAtividade = true (+2), participouAula = true (+1) => total 3
        int pontos = participacao.calcularPontos(true, true);
        assertEquals(3, pontos);
    }

    @Test
    void deveCalcularPontosComAmbosFalsos() {
        Participacao participacao = new Participacao();
        // entregouAtividade = false (+0), participouAula = false (+0) => total 0
        int pontos = participacao.calcularPontos(false, false);
        assertEquals(0, pontos);
    }
}