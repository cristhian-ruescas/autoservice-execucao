package com.autoservice.execucao.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ExecucaoMetrics {

    public static final String METRIC_ETAPA = "autoservice.execucao.etapa";
    public static final String METRIC_ERRO_API = "autoservice.execucao.api.erro";

    private final MeterRegistry meterRegistry;

    public ExecucaoMetrics(final MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
    }

    public void registrarEtapa(final String etapa) {
        meterRegistry.counter(METRIC_ETAPA, "etapa", etapa).increment();
    }

    public void registrarErroApi(final String tipo) {
        meterRegistry.counter(METRIC_ERRO_API, "tipo", tipo == null ? "desconhecido" : tipo).increment();
    }
}
