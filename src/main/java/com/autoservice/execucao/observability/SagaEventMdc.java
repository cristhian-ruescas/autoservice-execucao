package com.autoservice.execucao.observability;

import com.autoservice.execucao.messaging.EventoSaga;
import org.slf4j.MDC;

public final class SagaEventMdc {

    public static final String ORDEM_SERVICO_ID_MDC_KEY = "ordem_servico_id";

    private SagaEventMdc() {
    }

    public static void apply(final EventoSaga evento) {
        if (evento == null) {
            return;
        }
        if (evento.correlationId() != null) {
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, evento.correlationId().toString());
        }
        if (evento.ordemServicoId() != null) {
            MDC.put(ORDEM_SERVICO_ID_MDC_KEY, evento.ordemServicoId().toString());
        }
        DatadogTraceContext.applyToMdc(DatadogTraceContext.current());
    }

    public static void clear() {
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        MDC.remove(ORDEM_SERVICO_ID_MDC_KEY);
        DatadogTraceContext.clearFromMdc();
    }
}
