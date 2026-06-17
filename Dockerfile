# ──────────────────────────────────────────────────
# Stage 1: Build the Spring Boot application
# ──────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and config first for dependency caching
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon --quiet || true

# Copy source code
COPY src/ src/

# Build the bootable JAR
RUN ./gradlew bootJar --no-daemon --quiet

# ──────────────────────────────────────────────────
# Stage 2: Runtime image with JDK + NGINX + SSH + Git
# ──────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk

# Install NGINX, OpenSSH, Git, OpenSSL, and curl
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        nginx \
        openssh-server \
        openssl \
        ca-certificates \
        curl \
        git \
        default-mysql-client \
        python3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# ── NGINX setup ──
RUN mkdir -p /etc/nginx/ssl /var/log/nginx /run

# Generate self-signed SSL certificate
RUN openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/nginx-selfsigned.key \
    -out /etc/nginx/ssl/nginx-selfsigned.crt \
    -subj "/C=KH/ST=Phnom Penh/L=Phnom Penh/O=NxaSenpai/CN=localhost"

# Copy NGINX configuration
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# Remove default NGINX site config
RUN rm -f /etc/nginx/sites-enabled/default /etc/nginx/sites-available/default

# ── SSH setup ──
RUN mkdir -p /var/run/sshd && \
    echo 'root:root' | chpasswd && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    ssh-keygen -A

# ── Application setup ──
ENV JAVA_HOME=/opt/java/openjdk
RUN mkdir -p /app /app/uploads/photos

# Copy pre-built JAR from build stage (for immediate startup)
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Copy full project source + git history (for Ansible: git pull, gradle build, tests)
COPY .git/ /app/.git/
COPY gradlew gradlew.bat /app/
COPY gradle/ /app/gradle/
COPY build.gradle.kts settings.gradle.kts /app/
COPY src/ /app/src/

# Make gradlew executable in the project dir
RUN chmod +x /app/gradlew

# Copy startup script
COPY scripts/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

# Expose ports
# 8443 → NGINX HTTPS (website)
# 2222 → SSH
EXPOSE 8443 2222

# Healthcheck — verify Spring Boot is up
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -kf https://localhost:8443/api/health || exit 1

# Start everything
ENTRYPOINT ["/app/startup.sh"]
