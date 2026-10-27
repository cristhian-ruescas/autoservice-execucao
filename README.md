# autoservice-execucao

Fila de diagnóstico e reparo. O documento da fila fica no MongoDB (`fila_execucao`). O serviço não abre conexão com o PostgreSQL dos outros dois.

## Fluxo

- `diagnostico.solicitado` abre a fila em `EM_DIAGNOSTICO`.
- `execucao.solicitada` passa para o reparo.
- `compensacao.fila.liberar` marca a fila como `LIBERADA`.

Com `EXECUCAO_AUTOMATICA=true` (padrão do Compose) o diagnóstico e o reparo concluem sozinhos e publicam `diagnostico.concluido` e `execucao.concluida`. Com `EXECUCAO_FALHAR=true`, o reparo publica `execucao.falhou` para a OS compensar o pagamento.

Com os dois flags desligados, o mecânico usa:

- `POST /execucoes/{id}/diagnostico/concluir`
- `POST /execucoes/{id}/reparo/concluir`
- `POST /execucoes/{id}/falhar`

## Rodar

```bash
./mvnw verify
./mvnw spring-boot:run
```

Porta `8083`. Mongo local em `mongodb://localhost:27017/autoservice_execucao`. Swagger em `/swagger-ui.html`. OpenAPI em `openapi.yaml`. Collection em `postman/`.

A malha completa está no Compose de `autoservice-os`.

## Kubernetes

```bash
kubectl kustomize k8s
```

A URI do Mongo fica no Secret de exemplo, não no ConfigMap. O apply no cluster continua dependendo da AWS da fase 3.
