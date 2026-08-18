# ---------- Etapa 1: build ----------
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

# Copia primeiro os arquivos do Maven Wrapper para aproveitar cache de camadas
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Agora copia o restante do código e builda
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# O Render injeta a variável PORT automaticamente; o Spring Boot vai escutar nela
ENV SERVER_PORT=${PORT:-8080}

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]