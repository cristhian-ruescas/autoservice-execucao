package com.autoservice.execucao.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FilaRepository extends MongoRepository<FilaDocument, String> {

    long countByEtapa(String etapa);
}
