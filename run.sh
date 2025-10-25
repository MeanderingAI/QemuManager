#!/bin/bash

# QEMU Manager Launcher Script
# This script builds and runs the QEMU Manager application

echo "QEMU Manager Launcher"
echo "===================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2 | cut -d '.' -f 1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or higher is required"
    echo "Current Java version: $JAVA_VERSION"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Apache Maven"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn -version | head -n 1)"
echo ""

# Build the project if JAR doesn't exist or source is newer
JAR_FILE="target/qemu-manager-1.0.0.jar"
if [ ! -f "$JAR_FILE" ] || [ "src/main/java/com/qemumanager/QemuManagerApp.java" -nt "$JAR_FILE" ]; then
    echo "Building QEMU Manager..."
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo "Error: Build failed"
        exit 1
    fi
    echo "Build completed successfully"
else
    echo "Using existing JAR file"
fi

echo ""
echo "Starting QEMU Manager..."
echo "======================="

# Run the application
java -jar "$JAR_FILE"