package com.autoservice.execucao.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMensagemSagaTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publicaPeloTipo() {
        final RabbitMensagemSaga saga = new RabbitMensagemSaga(this.rabbitTemplate);
        final EventoSaga evento = EventoSaga.novo(TiposEvento.DIAGNOSTICO_CONCLUIDO, UUID.randomUUID(), UUID.randomUUID(), Map.of());

        saga.publicar(evento);

        verify(this.rabbitTemplate).convertAndSend(TiposEvento.DIAGNOSTICO_CONCLUIDO, evento);
    }
}
