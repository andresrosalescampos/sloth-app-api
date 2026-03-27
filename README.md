# sloth-life-chat-api

## Environment Configuration

This project contains settings specific to the corporate network (Nexus proxy + SSL inspection). When working on a personal machine without these restrictions, the following files need to be reverted.

---

### `settings.gradle`

**Corporate (current):**
```groovy
pluginManagement {
    repositories {
        maven { url 'https://nexus-proxy.almuk.santanderuk.corp/repository/maven-public' }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = 'api'
```

**Personal (revert to):**
```groovy
rootProject.name = 'api'
```
> Gradle uses the Plugin Portal by default; no need to declare `pluginManagement`.

---

### `build.gradle` — plugins block

**Corporate (current):** uses `buildscript {}` to resolve plugins as Maven dependencies directly from Nexus, bypassing the Gradle Plugin Portal.
```groovy
buildscript {
    repositories {
        maven { url 'https://nexus-proxy.almuk.santanderuk.corp/repository/maven-public' }
    }
    dependencies {
        classpath 'org.springframework.boot:spring-boot-gradle-plugin:4.0.4'
        classpath 'io.spring.gradle:dependency-management-plugin:1.1.7'
    }
}

apply plugin: 'java'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
```

**Personal (revert to):** standard Gradle DSL.
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.4'
    id 'io.spring.dependency-management' version '1.1.7'
}
```

---

### `build.gradle` — `repositories` block

**Corporate (current):**
```groovy
repositories {
    mavenLocal()
    maven { url 'https://nexus-proxy.almuk.santanderuk.corp/repository/maven-public' }
}
```

**Personal (revert to):**
```groovy
repositories {
    mavenCentral()
}
```

---

### `Dockerfile` — corporate certificate import

**Corporate (current):** both the build stage (to resolve dependencies from Nexus) and the run stage (to call the Gemini API at runtime) include a block to add the corporate CA to the JVM trust store.

Build stage — extracts cert from Nexus:
```dockerfile
RUN apk add --no-cache openssl && \
    openssl s_client -connect nexus-proxy.almuk.santanderuk.corp:443 -showcerts \
        </dev/null 2>/dev/null | \
    awk '/BEGIN CERTIFICATE/{c++; file="/tmp/cert-"c".pem"; inCert=1} inCert{print > file} /END CERTIFICATE/{inCert=0}' && \
    for cert in /tmp/cert-*.pem; do \
        keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit -noprompt -alias "corp-$(basename $cert .pem)" -file "$cert" 2>/dev/null || true; \
    done
```

Run stage — extracts cert from the Gemini API endpoint (as presented by the corporate proxy):
```dockerfile
RUN apk add --no-cache openssl && \
    openssl s_client -connect generativelanguage.googleapis.com:443 -showcerts \
        </dev/null 2>/dev/null | \
    awk '/BEGIN CERTIFICATE/{c++; file="/tmp/cert-"c".pem"; inCert=1} inCert{print > file} /END CERTIFICATE/{inCert=0}' && \
    for cert in /tmp/cert-*.pem; do \
        keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit -noprompt -alias "corp-$(basename $cert .pem)" -file "$cert" 2>/dev/null || true; \
    done && \
    apk del openssl
```

**Personal (remove both blocks):** the build stage becomes:
```dockerfile
FROM public.ecr.aws/docker/library/gradle:9.2.1-jdk21-alpine AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon
```
And the run stage requires no certificate setup.

---

## Gemini API Key Setup

The API key is provided via `src/main/resources/application-local.yaml`, which is excluded from version control (see `.gitignore`).

Create the file manually (or copy it from another machine):
```yaml
gemini:
  api:
    key: your_api_key_here
```

Obtain an API key from [Google AI Studio](https://aistudio.google.com/app/apikey).

When running with Docker, this file is bundled into the JAR during the image build, so no additional environment variable configuration is needed.

---

## Getting Started

### Run locally (IntelliJ / Gradle)

```bash
./gradlew bootRun
```

### Run with Docker

```bash
docker compose up --build -d
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`