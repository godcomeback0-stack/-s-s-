# Use official OpenJDK image
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/PayalBot-1.0.0.jar app.jar

# Copy application properties
COPY src/main/resources/application.properties application.properties

# Expose port (Render requires this)
EXPOSE 8080

# Environment variables
ENV BOT_TOKEN=${BOT_TOKEN}
ENV GEMINI_API_KEY=${GEMINI_API_KEY}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Run the bot
ENTRYPOINT ["java", "-jar", "app.jar"]