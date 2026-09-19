package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private ItemPedido item(String sku, long preco, int quantidade, int estoque, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, quantidade, estoque, peso, fragil);
    }

    @Test
    void deveRejeitarListaDeItensNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null));
    }

    @Test
    void deveAceitarNoLimiteDeCemItens() {
        ItemPedido item = item("SKU-1", 1_000, 1, 10, 500, false);
        List<ItemPedido> itens = Collections.nCopies(100, item);
        assertDoesNotThrow(() -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveRejeitarMaisDeCemItens() {
        ItemPedido item = item("SKU-1", 1_000, 1, 10, 500, false);
        List<ItemPedido> itens = Collections.nCopies(101, item);
        assertThrows(IllegalArgumentException.class, () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveRejeitarUfNula() {
        ItemPedido item = item("SKU-1", 1_000, 1, 10, 500, false);
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(item), null, false, null));
    }

    @Test
    void deveRejeitarUfComFormatoInvalido() {
        ItemPedido item = item("SKU-1", 1_000, 1, 10, 500, false);
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(item), "pr", false, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(item), "P", false, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(item), "PR1", false, null));
    }

    @Test
    void deveAceitarUfValida() {
        ItemPedido item = item("SKU-1", 1_000, 1, 10, 500, false);
        assertDoesNotThrow(() -> new Pedido(List.of(item), "PR", false, null));
    }

    @Test
    void subtotalIgnoraItensComQuantidadeZero() {
        ItemPedido ativo = item("A", 1_000, 2, 5, 500, false);
        ItemPedido inativo = item("B", 500, 0, 5, 500, false);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);
        assertEquals(2_000L, pedido.subtotalCentavos());
    }

    @Test
    void pesoGramasSomaApenasItensComQuantidadePositiva() {
        ItemPedido ativo = item("A", 1_000, 2, 5, 300, false);
        ItemPedido inativo = item("B", 500, 0, 5, 700, false);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);
        assertEquals(600, pedido.pesoGramas());
    }

    @Test
    void temFragilVerdadeiroApenasComItemFragilEQuantidadePositiva() {
        ItemPedido fragilAtivo = item("A", 1_000, 1, 5, 100, true);
        Pedido pedido = new Pedido(List.of(fragilAtivo), "PR", false, null);
        assertTrue(pedido.temFragil());
    }

    @Test
    void temFragilFalsoQuandoItemFragilTemQuantidadeZero() {
        ItemPedido fragilInativo = item("A", 1_000, 0, 5, 100, true);
        Pedido pedido = new Pedido(List.of(fragilInativo), "PR", false, null);
        assertFalse(pedido.temFragil());
    }

    @Test
    void temFragilFalsoQuandoNenhumItemEFragil() {
        ItemPedido naoFragil = item("A", 1_000, 1, 5, 100, false);
        Pedido pedido = new Pedido(List.of(naoFragil), "PR", false, null);
        assertFalse(pedido.temFragil());
    }

    @Test
    void estoqueSuficienteVerdadeiroQuandoTodosOsItensDisponiveis() {
        ItemPedido suficiente = item("A", 1_000, 2, 5, 100, false);
        Pedido pedido = new Pedido(List.of(suficiente), "PR", false, null);
        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void estoqueInsuficienteInterrompeNoPrimeiroItemSemEstoque() {
        ItemPedido insuficiente = item("A", 1_000, 6, 5, 100, false);
        ItemPedido suficiente = item("B", 1_000, 1, 5, 100, false);
        Pedido pedido = new Pedido(List.of(insuficiente, suficiente), "PR", false, null);
        assertFalse(pedido.estoqueSuficiente());
    }

    @Test
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> original = new ArrayList<>();
        original.add(item("A", 1_000, 1, 5, 100, false));
        Pedido pedido = new Pedido(original, "PR", false, null);

        original.add(item("B", 1_000, 1, 5, 100, false));

        assertEquals(1, pedido.itens().size());
    }
}
