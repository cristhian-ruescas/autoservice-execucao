# autoservice-execucao

Microsserviço da fila de diagnóstico e reparo. Persistência em **MongoDB** (`fila_execucao`). Não abre conexão com os PostgreSQL dos outros serviços.

## Fluxo

- `diagnostico.solicitado` → etapa `EM_DIAGNOSTICO`
- `execucao.solicitada` → reparo
- `compensacao.fila.liberar` → `LIBERADA`

Com `EXECUCAO_AUTOMATICA=true` (padrão do Compose), diagnóstico e reparo concluem sozinhos (`diagnostico.concluido` / `execucao.concluida`).

Com `EXECUCAO_FALHAR=true`, o reparo publica `execucao.falhou` para a OS compensar.

Com flags desligados, use:

- `POST /execucoes/{id}/diagnostico/concluir`
- `POST /execucoes/{id}/reparo/concluir`
- `POST /execucoes/{id}/falhar`

## Rodar

```bash
./mvnw verify
./mvnw spring-boot:run
```

Porta `8083`. Mongo local: `mongodb://localhost:27017/autoservice_execucao`.

Swagger: `/swagger-ui.html`. OpenAPI: `openapi.yaml`. Postman: `postman/`.

Malha completa: Compose em `autoservice-os/docker`.

Observabilidade (Datadog): hub em `autoservice-os/docs/observability/`.

## CI/CD e Kubernetes

Pipeline em `.github/workflows/` (testes, qualidade, imagem, deploy).

```bash
kubectl kustomize k8s
```

URI do Mongo fica no Secret de exemplo (`k8s/secret.example.yaml`), não no ConfigMap.
