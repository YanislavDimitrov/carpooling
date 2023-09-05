# Stage 1: Build
FROM gradle:7.2.0-jdk17 as build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon

# Stage 2: Deploy
FROM amazoncorretto:17.0.7-al2

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "/app/carpooling-0.0.1-SNAPSHOT.jar"]