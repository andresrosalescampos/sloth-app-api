# Build stage
FROM public.ecr.aws/docker/library/gradle:9.2.1-jdk21-alpine AS builder

WORKDIR /build

# Import corporate CA certificate chain into JVM trust store
RUN apk add --no-cache openssl && \
    openssl s_client -connect nexus-proxy.almuk.santanderuk.corp:443 -showcerts \
        </dev/null 2>/dev/null | \
    awk '/BEGIN CERTIFICATE/{c++; file="/tmp/cert-"c".pem"; inCert=1} inCert{print > file} /END CERTIFICATE/{inCert=0}' && \
    for cert in /tmp/cert-*.pem; do \
        keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit -noprompt -alias "corp-$(basename $cert .pem)" -file "$cert" 2>/dev/null || true; \
    done

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon

# Run stage
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre-alpine

LABEL maintainer="slothlife" \
      version="0.0.1-SNAPSHOT" \
      description="Sloth Life Chat API"

WORKDIR /app

# Import corporate CA certificate chain into JVM trust store
RUN apk add --no-cache openssl && \
    openssl s_client -connect generativelanguage.googleapis.com:443 -showcerts \
        </dev/null 2>/dev/null | \
    awk '/BEGIN CERTIFICATE/{c++; file="/tmp/cert-"c".pem"; inCert=1} inCert{print > file} /END CERTIFICATE/{inCert=0}' && \
    for cert in /tmp/cert-*.pem; do \
        keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit -noprompt -alias "corp-$(basename $cert .pem)" -file "$cert" 2>/dev/null || true; \
    done && \
    apk del openssl

# Create non-root user for security
RUN addgroup -S sloth && adduser -S sloth -G sloth
USER sloth:sloth

COPY --from=builder --chown=sloth:sloth /build/build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
