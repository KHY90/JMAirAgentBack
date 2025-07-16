FROM gradle:8.5-jdk21-jammy AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN ./gradlew build --no-daemon -x test

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]