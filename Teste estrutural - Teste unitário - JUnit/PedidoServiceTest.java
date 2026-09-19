package br.edu.ifpr.pedidos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void deveLancarNullPointerParaPedidoOuClienteNulos() {
        PedidoService service = new PedidoService(total -> true);
        Cliente cliente = new Cliente(false, false, 0);
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 1, 5, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        assertThrows(NullPointerException.class, () -> service.fechar(null, cliente));
        assertThrows(NullPointerException.class, () -> service.fechar(pedido, null));
    }

    @Test
    void deveRetornarBloqueadoAntesDeAvaliarEstoqueOuCupom() {
        Cliente cliente = new Cliente(false, true, 0);
        ItemPedido item = new ItemPedido("SKU-1", 10_000, 5, 1, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, "CUPOM-INEXISTENTE");

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("BLOQUEADO", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertEquals(0L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveLancarExcecaoQuandoSubtotalDosItensAtivosForZero() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(), "PR", false, null);
        PedidoService service = new PedidoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente));
    }

    @Test
    void deveRetornarSemEstoqueAntesDeAplicarOuValidarOCupom() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("SKU-1", 10_000, 5, 1, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, "CUPOM-INEXISTENTE");

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("SEM_ESTOQUE", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertEquals(0L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveRetornarRevisaoQuandoAnaliseDeRiscoNaoAprovaSemCobrar() {
        Cliente cliente = new Cliente(false, false, 0);
        ItemPedido item = new ItemPedido("SKU-1", 200_000, 1, 1, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("REVISAO", resultado.status()),
            () -> assertEquals(200_000L, resultado.subtotalCentavos()),
            () -> assertEquals(10_000L, resultado.descontoCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertEquals(190_000L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveRetornarPagamentoRecusadoQuandoProcessadorNegaComRiscoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("SKU-1", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return false;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void deveTentarNovamenteAposIndisponibilidadeTemporariaEAprovarPagamento() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("SKU-1", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> chamadas = new ArrayList<>();
        Deque<Object> respostas = new ArrayDeque<>(List.of(
            new IllegalStateException("indisponível"), true));
        PedidoService service = new PedidoService(total -> {
            chamadas.add(total);
            Object resposta = respostas.poll();
            if (resposta instanceof IllegalStateException excecao) throw excecao;
            return (Boolean) resposta;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertEquals("PAGO", resultado.status());
        assertEquals(2, chamadas.size());
    }
}
