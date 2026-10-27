package com.autoservice.execucao.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMensagemSaga implements MensagemSaga {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMensagemSaga(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(final EventoSaga evento) {
        this.rabbitTemplate.convertAndSend(evento.eventType(), evento);
    }
}
