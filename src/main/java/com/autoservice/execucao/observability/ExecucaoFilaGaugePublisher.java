package com.autoservice.execucao.observability;

import com.autoservice.execucao.domain.EtapaExecucao;
import com.autoservice.execucao.persistence.FilaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ExecucaoFilaGaugePublisher {

    public static final String METRIC_FILA_ATIVA = "autoservice.execucao.fila.ativa";

    private final MeterRegistry meterRegistry;
    private final FilaRepository filaRepository;
    private final AtomicLong filaAtiva = new AtomicLong();

    public ExecucaoFilaGaugePublisher(final MeterRegistry meterRegistry, final FilaRepository filaRepository) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
        this.filaRepository = Objects.requireNonNull(filaRepository);
    }

    @PostConstruct
    void registrarGauge() {
        Gauge.builder(METRIC_FILA_ATIVA, filaAtiva, AtomicLong::get)
                .description("Itens na fila de execucao que ainda nao foram liberados")
                .register(meterRegistry);
        publicar();
    }

    @Scheduled(fixedRateString = "${autoservice.metrics.publish-interval-ms:60000}")
    void publicar() {
        final long emDiagnostico = this.filaRepository.countByEtapa(EtapaExecucao.EM_DIAGNOSTICO.name());
        final long emReparo = this.filaRepository.countByEtapa(EtapaExecucao.EM_REPARO.name());
        final long diagnosticoConcluido = this.filaRepository.countByEtapa(EtapaExecucao.DIAGNOSTICO_CONCLUIDO.name());
        filaAtiva.set(emDiagnostico + emReparo + diagnosticoConcluido);
    }
}
