# Use OpenJDK 17 as the base image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

# Expose the port on which the application will run
EXPOSE 8761

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
