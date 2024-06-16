# Stage 1: Build the entire Maven project
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Build the project, including all modules
RUN mvn clean package

# Stage 2: Run the specific child module
FROM openjdk:17-jdk-slim

# Set the working directory

# Copy the built JAR of the child module from the build stage
COPY --from=build /app/edge/target/edge-shaded.jar /app/edge-shaded.jar


# Command to run the application
ENTRYPOINT [ "java", "-jar", "/app/edge-shaded.jar"]