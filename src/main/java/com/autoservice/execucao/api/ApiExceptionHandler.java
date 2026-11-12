package com.autoservice.execucao.api;

import com.autoservice.execucao.domain.NaoEncontradoException;
import com.autoservice.execucao.domain.NegocioException;
import com.autoservice.execucao.observability.ExecucaoMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ExecucaoMetrics metrics;

    public ApiExceptionHandler(final ExecucaoMetrics metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> negocio(final NegocioException exception) {
        this.metrics.registrarErroApi("negocio");
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(final NaoEncontradoException exception) {
        this.metrics.registrarErroApi("nao_encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }
}
