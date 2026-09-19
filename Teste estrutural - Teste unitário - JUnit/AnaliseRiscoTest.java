package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {

    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void deveRejeitarTotalNegativo() {
        Cliente cliente = new Cliente(false, false, 1);
        assertThrows(IllegalArgumentException.class, () -> risco.avaliar(cliente, -1, false));
    }

    @Test
    void clienteBloqueadoERecusadoIndependenteDoTotal() {
        Cliente cliente = new Cliente(false, true, 1);
        assertEquals("RECUSADO", risco.avaliar(cliente, 0, false));
    }

    @Test
    void semComprasAnterioresComTotalAcimaDoLimiteVaiParaRevisao() {
        Cliente cliente = new Cliente(false, false, 0);
        assertEquals("REVISAO", risco.avaliar(cliente, 100_001, false));
    }

    @Test
    void semComprasAnterioresNoLimiteExatoEAprovado() {
        Cliente cliente = new Cliente(false, false, 0);
        assertEquals("APROVADO", risco.avaliar(cliente, 100_000, false));
    }

    @Test
    void semComprasAnterioresComEntregaExpressaVaiParaRevisaoMesmoComTotalBaixo() {
        Cliente cliente = new Cliente(false, false, 0);
        assertEquals("REVISAO", risco.avaliar(cliente, 5_000, true));
    }

    @Test
    void semComprasAnterioresComTotalBaixoENaoExpressoEAprovado() {
        Cliente cliente = new Cliente(false, false, 0);
        assertEquals("APROVADO", risco.avaliar(cliente, 5_000, false));
    }

    @Test
    void comComprasAnterioresETotalAltoSemVipVaiParaRevisao() {
        Cliente cliente = new Cliente(false, false, 2);
        assertEquals("REVISAO", risco.avaliar(cliente, 500_001, false));
    }

    @Test
    void comComprasAnterioresNoLimiteExatoEAprovado() {
        Cliente cliente = new Cliente(false, false, 2);
        assertEquals("APROVADO", risco.avaliar(cliente, 500_000, false));
    }

    @Test
    void comComprasAnterioresClienteVipEAprovadoMesmoComTotalAlto() {
        Cliente cliente = new Cliente(true, false, 2);
        assertEquals("APROVADO", risco.avaliar(cliente, 600_000, false));
    }

    @Test
    void comComprasAnterioresETotalBaixoEAprovado() {
        Cliente cliente = new Cliente(false, false, 2);
        assertEquals("APROVADO", risco.avaliar(cliente, 100, false));
    }
}
