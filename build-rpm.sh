#!/bin/bash
# Linux RPM Build Script for Java WebView App
# Creates RPM package for Red Hat/Fedora

set -e

echo "========================================"
echo "Java WebView App - Linux RPM Builder"
echo "========================================"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

# Clean and build
echo "Building application..."
mvn clean package -q

# Create RPM package
echo "Creating RPM package..."
mvn jpackage:jpackage -P linux-rpm -q

echo "========================================"
echo "Build completed successfully!"
echo "RPM package created in: target/dist/"
echo "========================================"

# List created files
ls -la target/dist/*.rpm

echo "To install: sudo rpm -i java-webview-app-1.3.0-1.x86_64.rpm"
echo "Or: sudo dnf install java-webview-app-1.3.0-1.x86_64.rpm"