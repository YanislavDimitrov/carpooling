# Stage 1: Build
FROM gradle:7.2.0-jdk17 as build

WORKDIR /workspace/app

COPY build.gradle .
COPY src src

RUN gradle clean build

# Stage 2: Deploy
FROM amazoncorretto:17.0.7-al2

WORKDIR /app

ARG DEPENDENCY=/workspace/app/build

COPY --from=build ${DEPENDENCY} .

ENTRYPOINT ["java", "-jar", "/app/libs/carpooling-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080