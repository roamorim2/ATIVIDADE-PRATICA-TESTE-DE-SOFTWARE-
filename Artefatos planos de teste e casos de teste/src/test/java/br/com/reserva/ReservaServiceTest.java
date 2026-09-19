package br.com.reserva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Sala salaPadrao;
    private Turma turmaPadrao;
    private Usuario professorPadrao;
    private Usuario coordenador;

    @BeforeEach
    void setUp() {
        salaPadrao = new Sala("101", 40, false);
        turmaPadrao = new Turma("Turma A", 30);
        professorPadrao = new Usuario("Prof. Silva", Perfil.PROFESSOR);
        coordenador = new Usuario("Coord. Carlos", Perfil.COORDENACAO);
    }

    @Test
    @DisplayName("RF-01/RF-03/RF-05: Deve criar reserva com sucesso dentro das regras de horário e capacidade")
    void deveCriarReservaComSucesso() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fim = LocalTime.of(10, 0);

        when(reservaRepository.existeSobreposicao(salaPadrao, inicio, fim)).thenReturn(false);

        Reserva reserva = reservaService.reservarSala(salaPadrao, turmaPadrao, professorPadrao, inicio, fim);

        assertNotNull(reserva);
        assertEquals(salaPadrao, reserva.getSala());
        verify(auditoriaService, times(1)).registrarAcao(anyString());
    }

    @Test
    @DisplayName("RF-02/Risco Crítico: Deve lançar exceção ao tentar sobrepor horários na mesma sala")
    void deveLancarExcecaoAoSobreporHorario() {
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fim = LocalTime.of(12, 0);

        when(reservaRepository.existeSobreposicao(salaPadrao, inicio, fim)).thenReturn(true);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            reservaService.reservarSala(salaPadrao, turmaPadrao, professorPadrao, inicio, fim);
        });

        assertEquals("A sala já possui reserva no horário selecionado.", exception.getMessage());
    }

    @Test
    @DisplayName("RF-03/Risco Crítico: Deve impedir reserva se a turma for maior que a capacidade da sala")
    void deveImpedirReservaTurmaMaiorQueCapacidade() {
        Turma turmaGrande = new Turma("Turma B", 50);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            reservaService.reservarSala(salaPadrao, turmaGrande, professorPadrao, LocalTime.of(8, 0), LocalTime.of(10, 0));
        });

        assertEquals("Capacidade da sala é inferior ao tamanho da turma.", exception.getMessage());
    }

    @Test
    @DisplayName("RF-04: Deve bloquear reserva para sala em manutenção")
    void deveBloquearReservaEmSalaEmManutencao() {
        Sala salaManutencao = new Sala("102", 40, true);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            reservaService.reservarSala(salaManutencao, turmaPadrao, professorPadrao, LocalTime.of(8, 0), LocalTime.of(10, 0));
        });

        assertEquals("Sala indisponível devido a manutenção.", exception.getMessage());
    }

    @Test
    @DisplayName("RF-05: Deve impedir reservas fora do horário permitido (07h30 às 22h30)")
    void deveImpedirReservaForaDoHorarioPermitido() {
        LocalTime inicioInvalido = LocalTime.of(6, 30);
        LocalTime fimInvalido = LocalTime.of(8, 30);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            reservaService.reservarSala(salaPadrao, turmaPadrao, professorPadrao, inicioInvalido, fimInvalido);
        });

        assertEquals("Horário de reserva deve ser entre 07h30 e 22h30.", exception.getMessage());
    }

    @Test
    @DisplayName("RF-06/Risco Crítico: Professor não deve alterar reserva de outro professor")
    void naoDevePermitirProfessorAlterarReservaDeOutro() {
        Usuario outroProfessor = new Usuario("Prof. Souza", Perfil.PROFESSOR);
        Reserva reservaExistente = new Reserva(salaPadrao, turmaPadrao, professorPadrao, LocalTime.of(8, 0), LocalTime.of(10, 0));

        AcessoNegadoException exception = assertThrows(AcessoNegadoException.class, () -> {
            reservaService.alterarReserva(reservaExistente, outroProfessor, salaPadrao, LocalTime.of(10, 0), LocalTime.of(12, 0));
        });

        assertEquals("Apenas a coordenação pode alterar reservas de outros professores.", exception.getMessage());
    }

    @Test
    @DisplayName("RF-06/RF-08: Coordenação pode alterar reserva e deve disparar notificação")
    void devePermitirCoordenacaoAlterarReservaENotificar() {
        Reserva reservaExistente = new Reserva(salaPadrao, turmaPadrao, professorPadrao, LocalTime.of(8, 0), LocalTime.of(10, 0));

        reservaService.alterarReserva(reservaExistente, coordenador, salaPadrao, LocalTime.of(10, 0), LocalTime.of(12, 0));

        verify(notificacaoService, times(1)).enviarNotificacao(eq(professorPadrao), anyString());
        verify(auditoriaService, times(1)).registrarAcao(anyString());
    }

    @Test
    @DisplayName("RF-07/RF-08: Cancelamento deve liberar horário, registrar histórico e enviar notificação")
    void deveCancelarReservaELiberarHorario() {
        Reserva reservaExistente = new Reserva(salaPadrao, turmaPadrao, professorPadrao, LocalTime.of(8, 0), LocalTime.of(10, 0));

        reservaService.cancelarReserva(reservaExistente, professorPadrao);

        assertTrue(reservaExistente.isCancelada());
        verify(notificacaoService, times(1)).enviarNotificacao(eq(professorPadrao), anyString());
        verify(auditoriaService, times(1)).registrarAcao(anyString());
    }
}