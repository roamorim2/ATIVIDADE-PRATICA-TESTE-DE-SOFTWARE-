package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemValido() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 2, 10, 500, false);
        assertAll(
            () -> assertEquals("SKU-1", item.sku()),
            () -> assertEquals(2_000L, item.totalCentavos()),
            () -> assertTrue(item.disponivel())
        );
    }

    @Test
    void deveRejeitarSkuNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, 1_000, 1, 10, 500, false));
    }

    @Test
    void deveRejeitarSkuEmBranco() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("   ", 1_000, 1, 10, 500, false));
    }

    @Test
    void deveRejeitarPrecoZeroOuNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 0, 1, 10, 500, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", -10, 1, 10, 500, false));
    }

    @Test
    void deveAceitarPrecoNoLimiteMinimoEMaximo() {
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1, 1, 10, 500, false));
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1_000_000, 1, 10, 500, false));
    }

    @Test
    void deveRejeitarPrecoAcimaDoLimite() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000_001, 1, 10, 500, false));
    }

    @Test
    void deveRejeitarQuantidadeForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, -1, 10, 500, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 101, 10, 500, false));
    }

    @Test
    void deveAceitarQuantidadeNoLimiteMinimoEMaximo() {
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1_000, 0, 10, 500, false));
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1_000, 100, 200, 500, false));
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, -1, 500, false));
    }

    @Test
    void deveRejeitarPesoForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, 10, 0, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, 10, 100_001, false));
    }

    @Test
    void deveAceitarPesoNoLimiteMinimoEMaximo() {
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1_000, 1, 10, 1, false));
        assertDoesNotThrow(() -> new ItemPedido("SKU-1", 1_000, 1, 10, 100_000, false));
    }

    @Test
    void totalCentavosMultiplicaPrecoPelaQuantidade() {
        ItemPedido item = new ItemPedido("SKU-1", 1_500, 3, 10, 500, false);
        assertEquals(4_500L, item.totalCentavos());
    }

    @Test
    void itemInativoComQuantidadeZeroTemTotalZero() {
        ItemPedido item = new ItemPedido("SKU-1", 1_500, 0, 10, 500, false);
        assertEquals(0L, item.totalCentavos());
    }

    @Test
    void disponivelQuandoQuantidadeIgualAoEstoqueNoLimite() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 10, 10, 500, false);
        assertTrue(item.disponivel());
    }

    @Test
    void indisponivelQuandoQuantidadeExcedeEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 11, 10, 500, false);
        assertFalse(item.disponivel());
    }
}
