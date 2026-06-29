# ---- Etapa 1: Build ----
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Cachea dependencias: primero el pom, luego el codigo.
COPY pom.xml .
RUN apk add --no-cache maven && mvn -q dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn -q clean package -DskipTests --no-transfer-progress

# ---- Etapa 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/quicksplit-backend-1.0.0.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser
EXPOSE 8080

# wget viene en alpine y se usa para el healthcheck del compose.
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
