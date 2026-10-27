package com.autoservice.execucao.domain;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(final String message) {
        super(message);
    }
}
