# Stage 1: Build
FROM gradle:8.2.1-jdk17 as build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . /home/gradle/src
COPY src src

RUN gradle build

# Stage 2: Deploy
FROM amazoncorretto:17.0.7-al2

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/carpooling-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/carpooling-0.0.1-SNAPSHOT.jar"]