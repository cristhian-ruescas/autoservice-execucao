package com.autoservice.execucao.messaging;

import com.autoservice.execucao.service.ExecucaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecucaoEventoListenerTest {

    @Mock
    private ExecucaoService execucaoService;

    @Test
    void encaminhaEventosDaFila() {
        final ExecucaoEventoListener listener = new ExecucaoEventoListener(this.execucaoService);
        final EventoSaga diagnostico = EventoSaga.novo(TiposEvento.DIAGNOSTICO_SOLICITADO, UUID.randomUUID(), UUID.randomUUID(), Map.of());
        final EventoSaga reparo = EventoSaga.novo(TiposEvento.EXECUCAO_SOLICITADA, UUID.randomUUID(), UUID.randomUUID(), Map.of());
        final EventoSaga liberar = EventoSaga.novo(TiposEvento.COMPENSAR_FILA, UUID.randomUUID(), UUID.randomUUID(), Map.of());

        listener.receber(null);
        listener.receber(diagnostico);
        listener.receber(reparo);
        listener.receber(liberar);
        listener.receber(EventoSaga.novo("outro", UUID.randomUUID(), UUID.randomUUID(), Map.of()));

        verify(this.execucaoService).solicitarDiagnostico(diagnostico);
        verify(this.execucaoService).solicitarReparo(reparo);
        verify(this.execucaoService).liberar(liberar);
    }

    @Test
    void falhaNaoVoltaParaAFila() {
        final ExecucaoEventoListener listener = new ExecucaoEventoListener(this.execucaoService);
        final EventoSaga diagnostico = EventoSaga.novo(TiposEvento.DIAGNOSTICO_SOLICITADO, UUID.randomUUID(), UUID.randomUUID(), Map.of());
        doThrow(new IllegalStateException("boom")).when(this.execucaoService).solicitarDiagnostico(diagnostico);

        listener.receber(diagnostico);
    }
}
