# Mecanismos - demo de Resilience4j

Este projeto contém dois serviços Spring Boot para demonstrar Circuit Breaker, Retry e Bulkhead com Resilience4j.

Estrutura:
- `service-a`: cliente que chama `service-b` e aplica Resilience4j
- `service-b`: servidor simples que pode simular `ok`, `slow` e `error`
- `docker-compose.yml`: orquestra `mysql`, `service-b` e `service-a`

Como rodar (PowerShell):

```powershell
cd "C:/Users/Educação/Desktop/Facul/Mecanismos de Tolerância a Falhas/mecanismos"
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
O que falta / pontos a ajustar

Build dos JARs: Executar mvn -f service-b clean package -DskipTests e mvn -f service-a clean package -DskipTests antes de docker-compose build. Os Dockerfile fazem COPY target/*.jar, portanto precisam dos JARs gerados.
Dependências locais: Ter JDK 17 e Maven instalados na máquina de desenvolvimento para rodar os comandos Maven.
Docker e Docker Compose: Ter Docker Desktop / Docker Engine e Docker Compose instalados para usar docker-compose build e docker-compose up.
Healthcheck do service-b: docker-compose.yml usa CMD-SHELL curl -f http://localhost:8080/actuator/health || exit 1 dentro do container. A imagem base (eclipse-temurin:17-jdk-jammy) provavelmente não tem curl instalado — o healthcheck falhará até instalar curl. É necessário:
Instalar curl no Dockerfile (ex.: apt-get update; apt-get install -y curl) OU
Mudar o healthcheck para um comando presente no container (se houver) ou para testar externamente.
Possível dependência faltante (se ocorrer erro de compilação): Se o build falhar por não encontrar @CircuitBreaker, adicione explicitamente io.github.resilience4j:resilience4j-circuitbreaker:1.7.1 no pom.xml. (Normalmente resilience4j-spring-boot2 cobre isso, mas em alguns casos é necessário declarar o módulo de circuito.)

Checklist Rápido — Pré-requisitos

JDK: Instalar JDK 17.
Maven: Instalar Maven (para gerar os JARs).
Docker: Instalar Docker Desktop / Docker Engine.
Docker Compose: Disponível via Docker Desktop (ou docker-compose).
(Opcional) curl: Ter curl no host para testes; note que o container precisa de curl para o healthcheck (veja observação).

Ir para a raiz do projeto:
cd "C:/Users/Educação/Desktop/Facul/Mecanismos de Tolerância a Falhas/mecanismos"
Gerar os JARs (necessário antes do build das imagens):
mvn -f service-b clean package -DskipTests
mvn -f service-a clean package -DskipTests
Construir as imagens Docker:
docker-compose build
Subir os containers em background:
docker-compose up -d
Verificar containers e logs:
docker-compose ps
docker-compose logs -f service-a
# Em outra janela, pare com Ctrl+C quando terminar
Chamar o endpoint que encaminha de A → B:
curl http://localhost:8080/forward
Testar modos do service-b diretamente:
curl "http://localhost:8081/process?mode=ok"
curl "http://localhost:8081/process?mode=slow"
curl "http://localhost:8081/process?mode=error"
Simular indisponibilidade do service-b:
docker-compose stop service-b
curl http://localhost:8080/forward
# Reiniciar
docker-compose start service-b
