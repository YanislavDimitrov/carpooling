# Stage 1: Build
FROM maven:3.9.1-amazoncorretto-17 as build

WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN gradle clean build

# Stage 2: Deploy
FROM amazoncorretto:17.0.7-al2

WORKDIR /app

ARG DEPENDENCY=/workspace/app/build/libs

COPY --from=build ${DEPENDENCY} .

ENTRYPOINT ["java", "-jar", "/app/carpooling-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080