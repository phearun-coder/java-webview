#!/bin/bash

# Java WebView Desktop Application Launcher
# Cross-platform desktop app with Java backend and Web UI

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JAR_FILE="target/java-webview-app-1.3.0.jar"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║    Java WebView Desktop Application Launcher          ║${NC}"
echo -e "${BLUE}║    Native Desktop App with Embedded WebView          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Error: Java is not installed or not in PATH${NC}"
    echo -e "${YELLOW}  Please install Java 17 or higher:${NC}"
    echo -e "  - Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo -e "  - macOS: brew install openjdk@17"
    echo -e "  - Or download from: https://adoptium.net/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}✗ Error: Java 17 or higher is required${NC}"
    echo -e "  Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

echo -e "${GREEN}✓ Java detected: $(java -version 2>&1 | head -n 1)${NC}"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}⚠ JAR file not found. Building application...${NC}"
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}✗ Error: Maven is not installed${NC}"
        echo -e "${YELLOW}  Please install Maven:${NC}"
        echo -e "  - Ubuntu/Debian: sudo apt install maven"
        echo -e "  - macOS: brew install maven"
        echo -e "  - Or download from: https://maven.apache.org/download.cgi"
        exit 1
    fi
    
    # Build the application
    echo -e "${BLUE}Building with Maven...${NC}"
    mvn clean package -q
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Build failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Build successful${NC}"
fi

# Run the application
echo -e "${BLUE}Starting desktop application...${NC}"
echo ""

# Trap Ctrl+C to provide clean exit message
trap 'echo -e "\n${YELLOW}⚠ Application stopped${NC}"; exit 0' INT

# Run with JavaFX modules
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.web,javafx.fxml -jar "$JAR_FILE"
