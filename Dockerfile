# Stage 1: Build the application using a Maven container
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/task-api-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]