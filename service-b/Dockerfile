FROM eclipse-temurin:17-jdk-jammy

# Install curl for healthcheck and minimal ca-certificates
RUN apt-get update && apt-get install -y curl ca-certificates \ 
	&& rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
