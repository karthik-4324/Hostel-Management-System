# Build stage
FROM maven:3.8.5-openjdk-22 AS build
COPY . .
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:22.0.1-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]