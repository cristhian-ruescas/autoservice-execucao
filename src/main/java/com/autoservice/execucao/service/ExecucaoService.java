package com.autoservice.execucao.service;

import com.autoservice.execucao.api.ExecucaoResponse;
import com.autoservice.execucao.domain.EtapaExecucao;
import com.autoservice.execucao.domain.FilaExecucao;
import com.autoservice.execucao.domain.FilaStore;
import com.autoservice.execucao.domain.NaoEncontradoException;
import com.autoservice.execucao.domain.NegocioException;
import com.autoservice.execucao.messaging.EventoSaga;
import com.autoservice.execucao.messaging.MensagemSaga;
import com.autoservice.execucao.messaging.TiposEvento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ExecucaoService {

    private final FilaStore filaStore;
    private final MensagemSaga mensagemSaga;
    private final boolean automatica;
    private final boolean falharAutomatico;

    public ExecucaoService(
            final FilaStore filaStore,
            final MensagemSaga mensagemSaga,
            @Value("${autoservice.execucao.automatica:true}") final boolean automatica,
            @Value("${autoservice.execucao.falhar:false}") final boolean falharAutomatico
    ) {
        this.filaStore = filaStore;
        this.mensagemSaga = mensagemSaga;
        this.automatica = automatica;
        this.falharAutomatico = falharAutomatico;
    }

    public void solicitarDiagnostico(final EventoSaga evento) {
        if (evento == null || evento.ordemServicoId() == null) {
            return;
        }
        if (this.filaStore.porOrdem(evento.ordemServicoId()).isPresent()) {
            return;
        }
        final FilaExecucao fila = new FilaExecucao(
                evento.ordemServicoId(),
                evento.correlationId(),
                EventoSaga.relato(evento.payload()),
                EtapaExecucao.EM_DIAGNOSTICO,
                null,
                Instant.now()
        );
        this.filaStore.salvar(fila);
        if (this.automatica) {
            concluirDiagnostico(evento.ordemServicoId());
        }
    }

    public ExecucaoResponse concluirDiagnostico(final UUID ordemServicoId) {
        final FilaExecucao fila = buscar(ordemServicoId);
        if (fila.getEtapa() != EtapaExecucao.EM_DIAGNOSTICO) {
            return ExecucaoResponse.de(fila);
        }
        fila.setEtapa(EtapaExecucao.DIAGNOSTICO_CONCLUIDO);
        fila.setAtualizadoEm(Instant.now());
        this.filaStore.salvar(fila);
        publicar(TiposEvento.DIAGNOSTICO_CONCLUIDO, fila, Map.of());
        return ExecucaoResponse.de(fila);
    }

    public void solicitarReparo(final EventoSaga evento) {
        if (evento == null || evento.ordemServicoId() == null) {
            return;
        }
        final FilaExecucao fila = this.filaStore.porOrdem(evento.ordemServicoId()).orElseGet(() -> new FilaExecucao(
                evento.ordemServicoId(),
                evento.correlationId(),
                EventoSaga.relato(evento.payload()),
                EtapaExecucao.DIAGNOSTICO_CONCLUIDO,
                null,
                Instant.now()
        ));
        if (fila.getEtapa() == EtapaExecucao.REPARO_CONCLUIDO
                || fila.getEtapa() == EtapaExecucao.FALHOU
                || fila.getEtapa() == EtapaExecucao.LIBERADA) {
            return;
        }
        fila.setEtapa(EtapaExecucao.EM_REPARO);
        fila.setAtualizadoEm(Instant.now());
        this.filaStore.salvar(fila);
        if (this.falharAutomatico) {
            falhar(fila.getOrdemServicoId(), "falha automatica");
        } else if (this.automatica) {
            concluirReparo(fila.getOrdemServicoId());
        }
    }

    public ExecucaoResponse concluirReparo(final UUID ordemServicoId) {
        final FilaExecucao fila = buscar(ordemServicoId);
        if (fila.getEtapa() == EtapaExecucao.REPARO_CONCLUIDO) {
            return ExecucaoResponse.de(fila);
        }
        if (fila.getEtapa() == EtapaExecucao.FALHOU || fila.getEtapa() == EtapaExecucao.LIBERADA) {
            throw new NegocioException("Execucao encerrada");
        }
        fila.setEtapa(EtapaExecucao.REPARO_CONCLUIDO);
        fila.setAtualizadoEm(Instant.now());
        this.filaStore.salvar(fila);
        publicar(TiposEvento.EXECUCAO_CONCLUIDA, fila, Map.of());
        return ExecucaoResponse.de(fila);
    }

    public ExecucaoResponse falhar(final UUID ordemServicoId, final String motivo) {
        final FilaExecucao fila = buscar(ordemServicoId);
        if (fila.getEtapa() == EtapaExecucao.REPARO_CONCLUIDO || fila.getEtapa() == EtapaExecucao.LIBERADA) {
            throw new NegocioException("Execucao ja encerrada");
        }
        if (fila.getEtapa() == EtapaExecucao.FALHOU) {
            return ExecucaoResponse.de(fila);
        }
        final boolean diagnostico = fila.getEtapa() == EtapaExecucao.EM_DIAGNOSTICO;
        final String texto = motivo == null || motivo.isBlank() ? "falha na execucao" : motivo;
        fila.setEtapa(EtapaExecucao.FALHOU);
        fila.setMotivo(texto);
        fila.setAtualizadoEm(Instant.now());
        this.filaStore.salvar(fila);
        publicar(diagnostico ? TiposEvento.DIAGNOSTICO_FALHOU : TiposEvento.EXECUCAO_FALHOU, fila, Map.of("motivo", texto));
        return ExecucaoResponse.de(fila);
    }

    public void liberar(final EventoSaga evento) {
        if (evento == null || evento.ordemServicoId() == null) {
            return;
        }
        final FilaExecucao fila = this.filaStore.porOrdem(evento.ordemServicoId()).orElse(null);
        if (fila == null || fila.getEtapa() == EtapaExecucao.LIBERADA) {
            return;
        }
        fila.setEtapa(EtapaExecucao.LIBERADA);
        fila.setAtualizadoEm(Instant.now());
        this.filaStore.salvar(fila);
    }

    public ExecucaoResponse consultar(final UUID ordemServicoId) {
        return ExecucaoResponse.de(buscar(ordemServicoId));
    }

    private FilaExecucao buscar(final UUID ordemServicoId) {
        return this.filaStore.porOrdem(ordemServicoId)
                .orElseThrow(() -> new NaoEncontradoException("Execucao nao encontrada"));
    }

    private void publicar(final String tipo, final FilaExecucao fila, final Map<String, Object> payload) {
        this.mensagemSaga.publicar(EventoSaga.novo(tipo, fila.getOrdemServicoId(), fila.getCorrelationId(), payload));
    }
}
