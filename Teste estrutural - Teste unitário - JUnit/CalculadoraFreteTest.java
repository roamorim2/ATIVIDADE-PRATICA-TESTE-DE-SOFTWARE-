package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {

    private final CalculadoraFrete calculadora = new CalculadoraFrete();
    private static final Cliente COMUM = new Cliente(false, false, 1);
    private static final Cliente VIP = new Cliente(true, false, 1);

    private Pedido pedidoComPeso(String uf, boolean expresso, int pesoGramas, boolean fragil) {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 1, 10, pesoGramas, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveRejeitarLiquidoNegativo() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(pedido, COMUM, -1));
    }

    @Test
    void baseParanaSemPesoExcedenteEValorBaixo() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertEquals(1_200L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void baseSaoPauloERioDeJaneiroUsamMesmaTarifa() {
        Pedido sp = pedidoComPeso("SP", false, 1_000, false);
        Pedido rj = pedidoComPeso("RJ", false, 1_000, false);
        assertEquals(2_000L, calculadora.calcular(sp, COMUM, 10_000));
        assertEquals(2_000L, calculadora.calcular(rj, COMUM, 10_000));
    }

    @Test
    void baseDemaisUfsUsaTarifaPadrao() {
        Pedido pedido = pedidoComPeso("MG", false, 1_000, false);
        assertEquals(3_000L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void semExcedenteNoLimiteExatoDeDoisQuilos() {
        Pedido pedido = pedidoComPeso("PR", false, 2_000, false);
        assertEquals(1_200L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void umGramaAcimaDeDoisQuilosJaCobraUmaFracao() {
        Pedido pedido = pedidoComPeso("PR", false, 2_001, false);
        assertEquals(1_500L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void tresQuilosExatosCobraApenasUmaFracaoAdicional() {
        Pedido pedido = pedidoComPeso("PR", false, 3_000, false);
        assertEquals(1_500L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void umGramaAcimaDeTresQuilosCobraDuasFracoes() {
        Pedido pedido = pedidoComPeso("PR", false, 3_001, false);
        assertEquals(1_800L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void liquidoNoLimiteDeTrezentosReaisZeraFreteQuandoNaoExpresso() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertEquals(0L, calculadora.calcular(pedido, COMUM, 30_000));
    }

    @Test
    void liquidoLogoAbaixoDoLimiteNaoZeraFrete() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertEquals(1_200L, calculadora.calcular(pedido, COMUM, 29_999));
    }

    @Test
    void freteGratuitoNaoSeAplicaAPedidoExpresso() {
        Pedido pedido = pedidoComPeso("PR", true, 1_000, false);
        assertEquals(2_700L, calculadora.calcular(pedido, COMUM, 30_000));
    }

    @Test
    void clienteVipPagaMetadeDoFreteCalculado() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertEquals(600L, calculadora.calcular(pedido, VIP, 10_000));
    }

    @Test
    void clienteVipComFreteZeradoContinuaZero() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, false);
        assertEquals(0L, calculadora.calcular(pedido, VIP, 30_000));
    }

    @Test
    void pedidoExpressoAdicionaValorFixo() {
        Pedido pedido = pedidoComPeso("PR", true, 1_000, false);
        assertEquals(2_700L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void itemFragilAdicionaValorFixoUmaUnicaVez() {
        Pedido pedido = pedidoComPeso("PR", false, 1_000, true);
        assertEquals(1_700L, calculadora.calcular(pedido, COMUM, 10_000));
    }

    @Test
    void combinacaoDeTodosOsAdicionaisEIndependente() {
        Pedido pedido = pedidoComPeso("SP", true, 3_001, true);
        long esperado = ((2_000L + 600L) / 2) + 1_500L + 500L;
        assertEquals(esperado, calculadora.calcular(pedido, VIP, 10_000));
    }
}
