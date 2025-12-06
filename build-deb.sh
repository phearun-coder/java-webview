#!/bin/bash
# Linux DEB Build Script for Java WebView App
# Creates DEB package for Debian/Ubuntu

set -e

echo "========================================"
echo "Java WebView App - Linux DEB Builder"
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

# Create DEB package
echo "Creating DEB package..."
mvn jpackage:jpackage -P linux-deb -q

echo "========================================"
echo "Build completed successfully!"
echo "DEB package created in: target/dist/"
echo "========================================"

# List created files
ls -la target/dist/*.deb

echo "To install: sudo dpkg -i java-webview-app_1.3.0_amd64.deb"
echo "Or: sudo apt install ./java-webview-app_1.3.0_amd64.deb"