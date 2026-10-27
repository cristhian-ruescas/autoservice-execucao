package com.autoservice.execucao.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "fila_execucao")
public class FilaDocument {

    @Id
    private String ordemServicoId;
    private String correlationId;
    private String relato;
    private String etapa;
    private String motivo;
    private Instant atualizadoEm;

    public String getOrdemServicoId() {
        return this.ordemServicoId;
    }

    public void setOrdemServicoId(final String ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getCorrelationId() {
        return this.correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRelato() {
        return this.relato;
    }

    public void setRelato(final String relato) {
        this.relato = relato;
    }

    public String getEtapa() {
        return this.etapa;
    }

    public void setEtapa(final String etapa) {
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
