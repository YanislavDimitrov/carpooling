# Stage 1: Build
FROM maven:3.9.1-amazoncorretto-17 as build

WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN mvn clean package -P production

# Stage 2: Deploy
FROM amazoncorretto:17.0.7-al2

WORKDIR /app

ARG DEPENDENCY=/workspace/app/target

COPY --from=build ${DEPENDENCY} .

ENTRYPOINT ["java", "-jar", "/app/carpooling-1.0.0.jar"]

EXPOSE 8080