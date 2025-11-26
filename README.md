# Mecanismos - demo de Resilience4j

Este projeto contém dois serviços Spring Boot para demonstrar Circuit Breaker, Retry e Bulkhead com Resilience4j.

Estrutura:
- `service-a`: cliente que chama `service-b` e aplica Resilience4j
- `service-b`: servidor simples que pode simular `ok`, `slow` e `error`
- `docker-compose.yml`: orquestra `mysql`, `service-b` e `service-a`

Como rodar (PowerShell):

Nota: execute os comandos a partir da raiz do projeto (a pasta que contém `docker-compose.yml`).

```powershell
# A partir da raiz do projeto
mvn -f service-b clean package -DskipTests
mvn -f service-a clean package -DskipTests
docker-compose build
docker-compose up -d
```

Testes básicos:

```powershell
curl http://localhost:8080/forward
# Simular erro no service-b via - parar container
docker-compose stop service-b
curl http://localhost:8080/forward
# Reiniciar
docker-compose start service-b
```

Checklist Rápido — Pré-requisitos

JDK: Instalar JDK 17.
Maven: Instalar Maven (para gerar os JARs).
Docker: Instalar Docker Desktop / Docker Engine.
Docker Compose: Disponível via Docker Desktop (ou docker-compose).
(Opcional) curl: Ter curl no host para testes; note que o container precisa de curl para o healthcheck (veja observação).

Ir para a raiz do projeto

Abra um terminal na raiz do repositório (a pasta que contém `docker-compose.yml`) e execute os passos abaixo.

Gerar os JARs (necessário antes do build das imagens):

```powershell
mvn -f service-b clean package -DskipTests
mvn -f service-a clean package -DskipTests
```

Estrutura do repositório
- `service-a` — cliente que chama `service-b` e aplica Resilience4j (anotações e exemplo com decorators)
- `service-b` — servidor simples que responde em `/process` e pode simular `ok`, `slow` ou `error`
- `docker-compose.yml` — orquestra `mysql`, `service-b` e `service-a`

Pré-requisitos
- JDK 17
- Maven
- Docker Desktop / Docker Engine (inclui Docker Compose)

Quick start (PowerShell)

1. Abra um terminal na raiz do projeto (a pasta que contém `docker-compose.yml`).

2. Gerar os JARs (necessário antes de build das imagens):

```powershell
mvn -f service-b clean package -DskipTests
mvn -f service-a clean package -DskipTests
```

3. Build e subir containers:

```powershell
docker-compose build
docker-compose up -d
```

4. Verificar status e logs:

```powershell
docker-compose ps
docker-compose logs -f service-a
```

Endpoints principais
- `GET http://localhost:8080/forward` — endpoint em `service-a` que encaminha para `service-b` (usa anotações Resilience4j).
- `GET http://localhost:8080/forward-decorated` — exemplo programático com `Decorators` do Resilience4j.
- `GET http://localhost:8081/process?mode={ok|slow|error}` — endpoint em `service-b` para testar comportamentos.
- Actuator (em `service-a`): `GET http://localhost:8080/actuator/health` e `GET http://localhost:8080/actuator/metrics`.

Testes básicos

```powershell
# Chamada normal
curl http://localhost:8080/forward

# Testar service-b diretamente
curl "http://localhost:8081/process?mode=ok"
curl "http://localhost:8081/process?mode=slow"
curl "http://localhost:8081/process?mode=error"

# Simular indisponibilidade do service-b (para testar Circuit Breaker)
docker-compose stop service-b
curl http://localhost:8080/forward
# Reiniciar service-b
docker-compose start service-b
```

Verificação de health e logs

```powershell
# Verificar status dos containers
docker-compose ps
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}\t{{.Ports}}"

# Inspecionar o health report do container (Docker)
docker inspect --format='{{json .State.Health}}' mecanismos-service-b-1

# Consultar diretamente o actuator health do service-b
curl http://localhost:8081/actuator/health

# Ver logs em tempo real (service-a e service-b)
docker-compose logs -f service-a service-b

# Procurar mensagens relevantes de Resilience4j nos logs do service-a (PowerShell)
docker-compose logs service-a | Select-String "CircuitBreaker|Retry|Bulkhead|CallNotPermittedException"

# Ver métricas do actuator em service-a
curl http://localhost:8080/actuator/metrics
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:serviceB"
```

Notas importantes
- Os `Dockerfile` copiam os JARs de `target/*.jar`; garanta que os builds Maven foram executados antes de `docker-compose build`.
- As políticas Resilience4j estão configuradas em `service-a/src/main/resources/application.yml` (instância `serviceB`). O projeto demonstra duas formas de aplicação:
	- Anotações AOP: `@CircuitBreaker`, `@Retry`, `@Bulkhead` no método `forward()`.
	- Estilo funcional: `Decorators.ofSupplier(...)` em `forward-decorated()`.

Contribuindo / próximos passos
- Se desejar rodar testes mais avançados, experimente chamadas concorrentes para observar Bulkhead (com `maxConcurrentCalls` baixo) e forçar falhas para observar o ciclo `OPEN -> HALF_OPEN -> CLOSED` do Circuit Breaker.

Licença
- Este projeto é um exemplo educacional. Use livremente para fins de estudo.
