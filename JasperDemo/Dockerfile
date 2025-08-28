FROM openjdk:17-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu \
    fonts-dejavu-core \
    fonts-dejavu-extra \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/templates /app/data /app/out /app/logs /app/fonts

COPY target/*.jar app.jar

VOLUME ["/app/templates", "/app/data", "/app/out", "/app/logs", "/app/fonts"]

ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true"
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=UTC

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/api/reports/health || exit 1

RUN chmod -R 755 /app && \
    chmod -R 777 /app/out /app/logs

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]