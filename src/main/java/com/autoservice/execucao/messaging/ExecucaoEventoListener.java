package com.autoservice.execucao.messaging;

import com.autoservice.execucao.config.RabbitConfig;
import com.autoservice.execucao.observability.SagaEventMdc;
import com.autoservice.execucao.service.ExecucaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ExecucaoEventoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecucaoEventoListener.class);

    private final ExecucaoService execucaoService;

    public ExecucaoEventoListener(final ExecucaoService execucaoService) {
        this.execucaoService = execucaoService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receber(final EventoSaga evento) {
        if (evento == null || evento.eventType() == null) {
            return;
        }
        SagaEventMdc.apply(evento);
        try {
            switch (evento.eventType()) {
                case TiposEvento.DIAGNOSTICO_SOLICITADO -> this.execucaoService.solicitarDiagnostico(evento);
                case TiposEvento.EXECUCAO_SOLICITADA -> this.execucaoService.solicitarReparo(evento);
                case TiposEvento.COMPENSAR_FILA -> this.execucaoService.liberar(evento);
                default -> {
                }
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Falha ao tratar evento {}", evento.eventId(), exception);
        } finally {
            SagaEventMdc.clear();
        }
    }
}
