package com.autoservice.execucao.service;

import com.autoservice.execucao.domain.EtapaExecucao;
import com.autoservice.execucao.domain.FilaExecucao;
import com.autoservice.execucao.domain.FilaStore;
import com.autoservice.execucao.domain.NaoEncontradoException;
import com.autoservice.execucao.domain.NegocioException;
import com.autoservice.execucao.messaging.EventoSaga;
import com.autoservice.execucao.messaging.MensagemSaga;
import com.autoservice.execucao.messaging.TiposEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecucaoServiceTest {

    @Mock
    private MensagemSaga mensagemSaga;

    private final Memoria memoria = new Memoria();
    private final UUID ordemId = UUID.randomUUID();

    @Test
    void modoAutomaticoConcluiDiagnosticoEReparo() {
        final ExecucaoService service = service(true, false);

        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", "barulho")));
        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", "barulho")));
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, Map.of()));

        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("REPARO_CONCLUIDO");
        assertThat(tipos()).containsExactly(TiposEvento.DIAGNOSTICO_CONCLUIDO, TiposEvento.EXECUCAO_CONCLUIDA);
    }

    @Test
    void modoManualEsperaOMecanico() {
        final ExecucaoService service = service(false, false);

        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", " ")));
        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("EM_DIAGNOSTICO");
        assertThat(service.consultar(this.ordemId).relato()).isEmpty();

        service.concluirDiagnostico(this.ordemId);
        service.concluirDiagnostico(this.ordemId);
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, null));
        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("EM_REPARO");
        service.concluirReparo(this.ordemId);
        service.concluirReparo(this.ordemId);

        assertThat(tipos()).containsExactly(TiposEvento.DIAGNOSTICO_CONCLUIDO, TiposEvento.EXECUCAO_CONCLUIDA);
    }

    @Test
    void falhaAutomaticaNoReparo() {
        final ExecucaoService service = service(true, true);
        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", "freio")));
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, Map.of("relato", "freio")));

        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("FALHOU");
        assertThat(tipos()).contains(TiposEvento.EXECUCAO_FALHOU);
    }

    @Test
    void falhaNoDiagnosticoENoReparo() {
        final ExecucaoService service = service(false, false);
        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of()));
        assertThat(service.falhar(this.ordemId, " ").motivo()).isEqualTo("falha na execucao");
        assertThat(service.falhar(this.ordemId, "de novo").etapa()).isEqualTo("FALHOU");

        final UUID outra = UUID.randomUUID();
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, outra, Map.of("relato", "embreagem")));
        service.falhar(outra, "peca");

        assertThat(tipos()).contains(TiposEvento.DIAGNOSTICO_FALHOU, TiposEvento.EXECUCAO_FALHOU);
        verify(this.mensagemSaga, times(2)).publicar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void naoFalhaNemConcluiExecucaoEncerrada() {
        final ExecucaoService service = service(false, false);
        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", "x")));
        service.concluirDiagnostico(this.ordemId);
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, Map.of()));
        service.concluirReparo(this.ordemId);

        assertThatThrownBy(() -> service.falhar(this.ordemId, "tarde")).isInstanceOf(NegocioException.class);
        service.liberar(evento(TiposEvento.COMPENSAR_FILA, Map.of()));
        assertThatThrownBy(() -> service.concluirReparo(this.ordemId)).isInstanceOf(NegocioException.class);
        service.solicitarReparo(evento(TiposEvento.EXECUCAO_SOLICITADA, Map.of()));
        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("LIBERADA");
    }

    @Test
    void liberarEIgnorarAusencias() {
        final ExecucaoService service = service(false, false);
        service.solicitarDiagnostico(null);
        service.solicitarReparo(null);
        service.liberar(null);
        service.liberar(evento(TiposEvento.COMPENSAR_FILA, Map.of()));
        assertThatThrownBy(() -> service.consultar(this.ordemId)).isInstanceOf(NaoEncontradoException.class);

        service.solicitarDiagnostico(evento(TiposEvento.DIAGNOSTICO_SOLICITADO, Map.of("relato", "a")));
        service.liberar(evento(TiposEvento.COMPENSAR_FILA, Map.of()));
        service.liberar(evento(TiposEvento.COMPENSAR_FILA, Map.of()));
        assertThat(service.consultar(this.ordemId).etapa()).isEqualTo("LIBERADA");
    }

    private ExecucaoService service(final boolean automatica, final boolean falhar) {
        return new ExecucaoService(this.memoria, this.mensagemSaga, automatica, falhar);
    }

    private EventoSaga evento(final String tipo, final Map<String, Object> payload) {
        return evento(tipo, this.ordemId, payload);
    }

    private EventoSaga evento(final String tipo, final UUID id, final Map<String, Object> payload) {
        return EventoSaga.novo(tipo, id, UUID.randomUUID(), payload);
    }

    private List<String> tipos() {
        final ArgumentCaptor<EventoSaga> captor = ArgumentCaptor.forClass(EventoSaga.class);
        verify(this.mensagemSaga, org.mockito.Mockito.atLeast(0)).publicar(captor.capture());
        return new ArrayList<>(captor.getAllValues().stream().map(EventoSaga::eventType).toList());
    }

    private static final class Memoria implements FilaStore {
        private final Map<UUID, FilaExecucao> dados = new HashMap<>();

        @Override
        public FilaExecucao salvar(final FilaExecucao fila) {
            this.dados.put(fila.getOrdemServicoId(), fila);
            return fila;
        }

        @Override
        public Optional<FilaExecucao> porOrdem(final UUID ordemServicoId) {
            return Optional.ofNullable(this.dados.get(ordemServicoId));
        }
    }

    @Test
    void relatoVazioNoEvento() {
        assertThat(EventoSaga.relato(null)).isEmpty();
        assertThat(EventoSaga.relato(Map.of("relato", "ok"))).isEqualTo("ok");
        new FilaExecucao(this.ordemId, null, "r", EtapaExecucao.EM_DIAGNOSTICO, null, Instant.now()).setMotivo("m");
    }
}
