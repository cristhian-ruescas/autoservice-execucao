package com.autoservice.execucao.persistence;

import com.autoservice.execucao.domain.EtapaExecucao;
import com.autoservice.execucao.domain.FilaExecucao;
import com.autoservice.execucao.domain.FilaStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MongoFilaStore implements FilaStore {

    private final FilaRepository filaRepository;

    public MongoFilaStore(final FilaRepository filaRepository) {
        this.filaRepository = filaRepository;
    }

    @Override
    public FilaExecucao salvar(final FilaExecucao fila) {
        return paraDominio(this.filaRepository.save(paraDocumento(fila)));
    }

    @Override
    public Optional<FilaExecucao> porOrdem(final UUID ordemServicoId) {
        return this.filaRepository.findById(ordemServicoId.toString()).map(this::paraDominio);
    }

    private FilaDocument paraDocumento(final FilaExecucao fila) {
        final FilaDocument document = new FilaDocument();
        document.setOrdemServicoId(fila.getOrdemServicoId().toString());
        document.setCorrelationId(fila.getCorrelationId() == null ? null : fila.getCorrelationId().toString());
        document.setRelato(fila.getRelato());
        document.setEtapa(fila.getEtapa().name());
        document.setMotivo(fila.getMotivo());
        document.setAtualizadoEm(fila.getAtualizadoEm());
        return document;
    }

    private FilaExecucao paraDominio(final FilaDocument document) {
        return new FilaExecucao(
                UUID.fromString(document.getOrdemServicoId()),
                document.getCorrelationId() == null ? null : UUID.fromString(document.getCorrelationId()),
                document.getRelato(),
                EtapaExecucao.valueOf(document.getEtapa()),
                document.getMotivo(),
                document.getAtualizadoEm()
        );
    }
}
