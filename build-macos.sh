#!/bin/bash
# macOS Build Script for Java WebView App
# Creates DMG installer for macOS

set -e

echo "========================================"
echo "Java WebView App - macOS DMG Builder"
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

# Create DMG installer
echo "Creating DMG installer..."
mvn jpackage:jpackage -P macos -q

echo "========================================"
echo "Build completed successfully!"
echo "DMG installer created in: target/dist/"
echo "========================================"

# List created files
ls -la target/dist/*.dmg

echo "To install: Open the DMG file and drag the app to Applications folder"