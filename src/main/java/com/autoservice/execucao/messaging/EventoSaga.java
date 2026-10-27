package com.autoservice.execucao.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoSaga(
        UUID eventId,
        String eventType,
        UUID correlationId,
        UUID ordemServicoId,
        Map<String, Object> payload
) {

    public static EventoSaga novo(
            final String eventType,
            final UUID ordemServicoId,
            final UUID correlationId,
            final Map<String, Object> payload
    ) {
        return new EventoSaga(UUID.randomUUID(), eventType, correlationId, ordemServicoId, payload == null ? Map.of() : payload);
    }

    public static String relato(final Map<String, Object> payload) {
        if (payload == null || payload.get("relato") == null || payload.get("relato").toString().isBlank()) {
            return "";
        }
        return payload.get("relato").toString();
    }
}
