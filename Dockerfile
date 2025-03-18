# Use a smaller JDK/JRE image for better performance
FROM eclipse-temurin:21-jre as builder

# Set working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY ./target/task-management-app-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
