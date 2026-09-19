package br.edu.ifpr.pedidos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    @Test
    void deveRejeitarTotalZeroOuNegativo() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 3));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(-1, 3));
    }

    @Test
    void deveRejeitarLimiteDeTentativasForaDoIntervalo() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 0));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 4));
    }

    @Test
    void deveAceitarLimitesDeTentativasNoIntervaloValido() {
        PagamentoService service = new PagamentoService(total -> true);
        assertDoesNotThrow(() -> service.pagar(1_000, 1));
        assertDoesNotThrow(() -> service.pagar(1_000, 3));
    }

    @Test
    void deveAprovarNaPrimeiraTentativa() {
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.add(total);
            return true;
        });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(List.of(1_000L), chamadas);
    }

    @Test
    void deveRecusarDefinitivamenteSemRepetirQuandoProcessadorRetornaFalso() {
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.add(total);
            return false;
        });

        assertFalse(service.pagar(1_000, 3));
        assertEquals(1, chamadas.size());
    }

    @Test
    void deveRepetirAposIndisponibilidadeTemporariaEDepoisAprovar() {
        Deque<Object> respostas = new ArrayDeque<>(List.of(
            new IllegalStateException("indisponível"), true));
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.add(total);
            Object resposta = respostas.poll();
            if (resposta instanceof IllegalStateException excecao) throw excecao;
            return (Boolean) resposta;
        });

        assertTrue(service.pagar(2_000, 3));
        assertEquals(2, chamadas.size());
    }

    @Test
    void deveEsgotarTentativasEDevolverFalsoQuandoSempreIndisponivel() {
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.add(total);
            throw new IllegalStateException("indisponível");
        });

        assertFalse(service.pagar(3_000, 3));
        assertEquals(3, chamadas.size());
    }

    @Test
    void devePropagarExcecoesQueNaoSaoIndisponibilidadeTemporaria() {
        PagamentoService service = new PagamentoService(total -> {
            throw new RuntimeException("erro inesperado");
        });

        assertThrows(RuntimeException.class, () -> service.pagar(1_000, 3));
    }

    @Test
    void deveRespeitarLimiteDeUmaUnicaTentativa() {
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.add(total);
            throw new IllegalStateException("indisponível");
        });

        assertFalse(service.pagar(1_000, 1));
        assertEquals(1, chamadas.size());
    }
}
