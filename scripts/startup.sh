#!/bin/bash
set -e

echo "=== Starting SSH daemon on port 2222 ==="
/usr/sbin/sshd -p 2222

echo "=== Starting NGINX ==="
nginx -g 'daemon off;' &

echo "=== Starting Spring Boot on port 8080 ==="
exec java -jar /app/app.jar
