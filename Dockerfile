# Run: copy the pre-built JAR (run './gradlew bootJar --no-daemon' locally before building this image)
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre-alpine

LABEL maintainer="slothlife" \
      version="0.0.1-SNAPSHOT" \
      description="Sloth Life API"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S sloth && adduser -S sloth -G sloth
USER sloth:sloth

# Copy the pre-built JAR
COPY --chown=sloth:sloth build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
