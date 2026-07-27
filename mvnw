#!/bin/sh
# Maven Wrapper - simplified version
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.6"
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven..."
    mkdir -p "$MAVEN_HOME"
    cd /tmp
    wget -q "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz" -O maven.tar.gz
    tar xzf maven.tar.gz -C "$MAVEN_HOME" --strip-components=1
    rm maven.tar.gz
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
