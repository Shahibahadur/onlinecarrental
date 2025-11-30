FROM mcr.microsoft.com/devcontainers/java:21

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

WORKDIR /workspace

# Preload dependencies
COPY pom.xml .
RUN mvn -q dependency:resolve || true

COPY . .
