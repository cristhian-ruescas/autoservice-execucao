# autoservice-execucao — Fase 4

Microsserviço da **fila de diagnóstico e reparo**. Persistência em **MongoDB** (`fila_execucao`). Sem conexão com os PostgreSQL dos outros serviços.

Parte da entrega **Fase 4** (saga + compensação).

## Qualidade

| Item | Status |
|------|--------|
| Testes | `./mvnw verify` |
| Cobertura | JaCoCo **≥ 80%** (gate no build) |
| OpenAPI | [`openapi.yaml`](./openapi.yaml) · Swagger `/swagger-ui.html` |
| Postman | `postman/` |
| CI/CD | `.github/workflows/ci-cd.yml` — Test → Docker → Deploy Kind + health |
| Lint / qualidade | pipeline + Sonar (quando `SONAR_TOKEN` presente) |

## Fluxo

- `diagnostico.solicitado` → etapa `EM_DIAGNOSTICO`
- `execucao.solicitada` → reparo
- `compensacao.fila.liberar` → `LIBERADA`

Flags:

| Variável | Efeito |
|----------|--------|
| `EXECUCAO_AUTOMATICA=true` | Diagnóstico/reparo concluem sozinhos (Compose) |
| `EXECUCAO_AUTOMATICA=false` | Demo manual pelos endpoints abaixo |
| `EXECUCAO_FALHAR=true` | Reparo publica `execucao.falhou` (compensa) |

Endpoints manuais:

- `POST /execucoes/{id}/diagnostico/concluir`
- `POST /execucoes/{id}/reparo/concluir`
- `POST /execucoes/{id}/falhar`
- `GET /execucoes/{id}`

## Rodar

```bash
./mvnw verify
./mvnw spring-boot:run
```

Porta `8083`. Mongo local: `mongodb://localhost:27017/autoservice_execucao`.

Malha completa: Compose em `autoservice-os/docker`.

Observabilidade: hub em `autoservice-os/docs/observability/`.

## CI/CD e Kubernetes

```bash
kubectl kustomize k8s
```

URI do Mongo no Secret de exemplo (`k8s/secret.example.yaml`), não no ConfigMap.
