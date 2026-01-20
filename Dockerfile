# Build stage - Use Maven with JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all files
COPY . .

# Grant execution permission to the Maven Wrapper
RUN chmod +x mvnw

# Build the app using JDK 21
RUN ./mvnw clean package -DskipTests

# Run stage - Use JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]