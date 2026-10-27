package com.autoservice.execucao.api;

import com.autoservice.execucao.domain.FilaExecucao;

import java.time.Instant;
import java.util.UUID;

public record ExecucaoResponse(
        UUID ordemServicoId,
        UUID correlationId,
        String relato,
        String etapa,
        String motivo,
        Instant atualizadoEm
) {

    public static ExecucaoResponse de(final FilaExecucao fila) {
        return new ExecucaoResponse(
                fila.getOrdemServicoId(),
                fila.getCorrelationId(),
                fila.getRelato(),
                fila.getEtapa().name(),
                fila.getMotivo(),
                fila.getAtualizadoEm()
        );
    }
}
