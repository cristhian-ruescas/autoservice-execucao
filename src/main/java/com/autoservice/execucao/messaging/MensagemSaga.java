package com.autoservice.execucao.messaging;

public interface MensagemSaga {

    void publicar(EventoSaga evento);
}
