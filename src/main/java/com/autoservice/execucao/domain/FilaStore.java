package com.autoservice.execucao.domain;

import java.util.Optional;
import java.util.UUID;

public interface FilaStore {

    FilaExecucao salvar(FilaExecucao fila);

    Optional<FilaExecucao> porOrdem(UUID ordemServicoId);
}
