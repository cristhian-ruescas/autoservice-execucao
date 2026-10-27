package com.autoservice.execucao.domain;

import java.time.Instant;
import java.util.UUID;

public class FilaExecucao {

    private final UUID ordemServicoId;
    private final UUID correlationId;
    private final String relato;
    private EtapaExecucao etapa;
    private String motivo;
    private Instant atualizadoEm;

    public FilaExecucao(
            final UUID ordemServicoId,
            final UUID correlationId,
            final String relato,
            final EtapaExecucao etapa,
            final String motivo,
            final Instant atualizadoEm
    ) {
        this.ordemServicoId = ordemServicoId;
        this.correlationId = correlationId;
        this.relato = relato;
        this.etapa = etapa;
        this.motivo = motivo;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getOrdemServicoId() {
        return this.ordemServicoId;
    }

    public UUID getCorrelationId() {
        return this.correlationId;
    }

    public String getRelato() {
        return this.relato;
    }

    public EtapaExecucao getEtapa() {
        return this.etapa;
    }

    public void setEtapa(final EtapaExecucao etapa) {
        this.etapa = etapa;
    }

    public String getMotivo() {
        return this.motivo;
    }

    public void setMotivo(final String motivo) {
        this.motivo = motivo;
    }

    public Instant getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public void setAtualizadoEm(final Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
