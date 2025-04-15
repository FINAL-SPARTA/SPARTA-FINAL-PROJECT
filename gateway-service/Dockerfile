FROM gradle:8.3-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 19091
ENTRYPOINT ["java", "-jar", "app.jar"]
