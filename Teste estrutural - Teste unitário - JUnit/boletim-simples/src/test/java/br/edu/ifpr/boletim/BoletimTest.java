package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(8);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveDeixarAlunoEmRecuperacao() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(5.0);
        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveRecuperarNotaAlunoComMediaQuatro() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(4);
        assertEquals("RECUPERACAO", resultado);
    }
    @Test
    void deveCalcularMediaCorretamente() {
        Boletim boletim = new Boletim();
        double media = boletim.calcularMedia(7.5, 8.0);
        assertEquals(7.75, media, 0.0001);
    }
    @Test
    void deveContarZeroAprovadosQuandoArrayVazio() {
        Boletim boletim = new Boletim();
        int quantidade = boletim.contarAprovados(new double[]{});
        assertEquals(0, quantidade);
    }

    @Test
    void deveContarAprovadosCorretamente() {
        Boletim boletim = new Boletim();
        // Array com notas: 8.0 (aprovado), 5.0 (recuperação), 7.0 (aprovado) -> Deve retornar 2
        int quantidade = boletim.contarAprovados(new double[]{8.0, 5.0, 7.0});
        assertEquals(2, quantidade);
    }
}