# Stage 1: compile and package the WAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source so rebuilds are faster
COPY pom.xml .
COPY .mvn/ .mvn/
RUN mvn dependency:go-offline -B

# Build the WAR
COPY src/ src/
RUN mvn clean package -DskipTests -B

# Stage 2: run on GlassFish 8 (Jakarta EE 11 / Servlet 6.1 — the assignment's mandated stack)
# Needs the full JDK, not just a JRE: GlassFish compiles JSPs to servlets with javac at request time.
FROM eclipse-temurin:21-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip curl \
    && curl -fsSL -o /tmp/glassfish.zip https://github.com/eclipse-ee4j/glassfish/releases/download/8.0.3/glassfish-8.0.3.zip \
    && unzip -q /tmp/glassfish.zip -d /opt \
    && rm /tmp/glassfish.zip \
    && apt-get purge -y unzip curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/speakout.war /opt/speakout.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
