package com.autoservice.execucao.persistence;

import com.autoservice.execucao.domain.EtapaExecucao;
import com.autoservice.execucao.domain.FilaExecucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoFilaStoreTest {

    @Mock
    private FilaRepository filaRepository;

    @Test
    void converteDocumentoEDominio() {
        final MongoFilaStore store = new MongoFilaStore(this.filaRepository);
        final UUID ordemId = UUID.randomUUID();
        final UUID correlationId = UUID.randomUUID();
        final Instant agora = Instant.parse("2026-10-20T12:00:00Z");
        when(this.filaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final FilaExecucao salva = store.salvar(new FilaExecucao(
                ordemId, correlationId, "relato", EtapaExecucao.EM_REPARO, "motivo", agora
        ));

        assertThat(salva.getOrdemServicoId()).isEqualTo(ordemId);
        assertThat(salva.getCorrelationId()).isEqualTo(correlationId);
        assertThat(salva.getEtapa()).isEqualTo(EtapaExecucao.EM_REPARO);
        assertThat(salva.getMotivo()).isEqualTo("motivo");

        final FilaDocument semCorrelation = new FilaDocument();
        semCorrelation.setOrdemServicoId(ordemId.toString());
        semCorrelation.setEtapa(EtapaExecucao.LIBERADA.name());
        semCorrelation.setRelato("r");
        semCorrelation.setAtualizadoEm(agora);
        when(this.filaRepository.findById(ordemId.toString())).thenReturn(Optional.of(semCorrelation));

        assertThat(store.porOrdem(ordemId)).get().extracting(FilaExecucao::getCorrelationId).isNull();
        assertThat(store.salvar(new FilaExecucao(ordemId, null, "r", EtapaExecucao.LIBERADA, null, agora)).getCorrelationId()).isNull();
    }
}
