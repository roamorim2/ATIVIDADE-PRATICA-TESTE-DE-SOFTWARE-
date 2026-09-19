package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {

    private final PoliticaDesconto politica = new PoliticaDesconto();
    private final Cliente comumComHistorico = new Cliente(false, false, 3);
    private final Cliente comumSemHistorico = new Cliente(false, false, 0);
    private final Cliente vip = new Cliente(true, false, 3);

    @Test
    void deveRejeitarSubtotalNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(comumComHistorico, -1, null));
    }

    @Test
    void clienteVipRecebeDezPorCento() {
        assertEquals(10_000L, politica.calcular(vip, 100_000, null));
    }

    @Test
    void clienteComumComSubtotalNoLimiteRecebeCincoPorCento() {
        assertEquals(2_500L, politica.calcular(comumComHistorico, 50_000, null));
    }

    @Test
    void clienteComumLogoAbaixoDoLimiteNaoRecebeDesconto() {
        assertEquals(0L, politica.calcular(comumComHistorico, 49_999, null));
    }

    @Test
    void cupomNuloMantemDescontoBase() {
        assertEquals(0L, politica.calcular(comumComHistorico, 10_000, null));
    }

    @Test
    void cupomEmBrancoMantemDescontoBase() {
        assertEquals(0L, politica.calcular(comumComHistorico, 10_000, "   "));
    }

    @Test
    void cupomBemVindoENormalizadoComEspacosEMinusculas() {
        assertEquals(2_000L, politica.calcular(comumSemHistorico, 10_000, "  bemvindo  "));
    }

    @Test
    void cupomBemVindoNaoAplicaComHistoricoAnterior() {
        assertEquals(0L, politica.calcular(comumComHistorico, 10_000, "BEMVINDO"));
    }

    @Test
    void cupomBemVindoNaoAplicaAbaixoDoSubtotalMinimo() {
        assertEquals(0L, politica.calcular(comumSemHistorico, 9_999, "BEMVINDO"));
    }

    @Test
    void cupomExtra10SomaDezPorCentoAcimaDoLimite() {
        assertEquals(2_000L, politica.calcular(comumComHistorico, 20_000, "EXTRA10"));
    }

    @Test
    void cupomExtra10NaoAplicaAbaixoDoLimite() {
        assertEquals(0L, politica.calcular(comumComHistorico, 19_999, "EXTRA10"));
    }

    @Test
    void cupomDesconhecidoLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(comumComHistorico, 10_000, "NAOEXISTE"));
    }

    @Test
    void descontoCombinadoRespeitaTetoDeVintePorCento() {
        Cliente vipSemHistorico = new Cliente(true, false, 0);
        assertEquals(2_000L, politica.calcular(vipSemHistorico, 10_000, "BEMVINDO"));
    }
}
