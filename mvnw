#!/bin/sh
# Maven Wrapper script
# This script downloads and runs the Maven wrapper

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    mkdir -p .mvn/wrapper
    curl -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null || wget -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
