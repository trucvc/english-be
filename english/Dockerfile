# Build stage using Maven and JDK 23
FROM maven:3-openjdk-23 AS build
WORKDIR /app

# Copy the application source code and build it
COPY . .
RUN mvn clean package -DskipTests

# Run stage using OpenJDK 23 runtime
FROM openjdk:23-jdk-slim
WORKDIR /app

# Copy the WAR file from the build stage to the runtime stage
COPY --from=build /app/target/DrComputer-0.0.1-SNAPSHOT.war drcomputer.war

# Expose port 8080
EXPOSE 8080

# Set entrypoint to run the WAR file using Java 23
ENTRYPOINT ["java", "-jar", "drcomputer.war"]
