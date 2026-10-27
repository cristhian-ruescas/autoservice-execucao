package com.autoservice.execucao.domain;

public class NegocioException extends RuntimeException {

    public NegocioException(final String message) {
        super(message);
    }
}
