# Setup Guide

## Quick Start

### 1. Prerequisites

- **Java 17+** ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- Modern web browser (Chrome, Firefox, Edge, Safari)

### 2. Build the Application

```bash
# Clone or navigate to the project directory
cd java-webview

# Build with Maven
mvn clean package

# This creates: target/java-webview-app-1.0.0.jar
```

### 3. Run the Application

#### Option A: Using Scripts (Recommended)

**Linux/macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Windows:**
```cmd
run.bat
```

#### Option B: Direct Execution

```bash
java -jar target/java-webview-app-1.0.0.jar
```

#### Option C: Using Maven

```bash
mvn exec:java -Dexec.mainClass="com.example.app.Application"
```

### 4. Access the Application

The application will automatically try to open your system browser. If it doesn't open automatically:

**Manual Access:**
- Open your browser
- Navigate to: **http://localhost:8080**

## Port Configuration

If port 8080 is already in use, you can change it:

1. Edit `src/main/java/com/example/app/Application.java`
2. Change the `BACKEND_PORT` constant:
   ```java
   private static final int BACKEND_PORT = 9090; // or any available port
   ```
3. Rebuild: `mvn clean package`

## Platform-Specific Notes

### Linux

On Linux systems, the application tries to use `xdg-open` to launch the browser. If you're in a containerized environment (like GitHub Codespaces or Docker), the browser won't open automatically, but the server will still run. Simply access it manually at http://localhost:8080

### macOS

Works out of the box with `java.awt.Desktop` API.

### Windows

Works out of the box with `java.awt.Desktop` API.

## Troubleshooting

### Issue: "Address already in use"

**Solution:** Change the port number (see Port Configuration above) or stop the process using port 8080:

**Linux/macOS:**
```bash
# Find the process
lsof -i :8080

# Kill it
kill -9 <PID>
```

**Windows:**
```cmd
# Find the process
netstat -ano | findstr :8080

# Kill it
taskkill /PID <PID> /F
```

### Issue: Browser doesn't open automatically

**Solution:** This is normal in containerized environments. Manually open your browser to http://localhost:8080

### Issue: "Cannot find Java"

**Solution:** Install Java 17 or higher:
- **Linux:** `sudo apt install openjdk-17-jdk`
- **macOS:** `brew install openjdk@17`
- **Windows:** Download from [Adoptium](https://adoptium.net/)

### Issue: "Cannot find Maven"

**Solution:** Install Maven:
- **Linux:** `sudo apt install maven`
- **macOS:** `brew install maven`
- **Windows:** Download from [Maven Website](https://maven.apache.org/download.cgi)

### Issue: Build fails with dependency errors

**Solution:** Clean Maven cache and rebuild:
```bash
mvn clean
rm -rf ~/.m2/repository
mvn package
```

## Development Mode

For development with auto-reload:

```bash
# Terminal 1: Run backend with Maven
mvn compile exec:java -Dexec.mainClass="com.example.app.Application"

# Edit frontend files in src/main/resources/webview/
# Changes to HTML/CSS/JS are immediately visible on browser refresh
# Java changes require Maven restart
```

## Building for Distribution

### Create Standalone JAR

The build already creates a fat JAR with all dependencies:
```bash
mvn clean package
# Output: target/java-webview-app-1.0.0.jar
```

### Create Native Installer (Java 14+)

Using jpackage:

**Linux:**
```bash
jpackage --input target \
  --name "Java WebView App" \
  --main-jar java-webview-app-1.0.0.jar \
  --type deb
```

**macOS:**
```bash
jpackage --input target \
  --name "Java WebView App" \
  --main-jar java-webview-app-1.0.0.jar \
  --type dmg
```

**Windows:**
```cmd
jpackage --input target ^
  --name "Java WebView App" ^
  --main-jar java-webview-app-1.0.0.jar ^
  --type msi
```

## Testing the API

### Using curl

```bash
# Health check
curl http://localhost:8080/api/health

# Get data
curl http://localhost:8080/api/data

# Calculate
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1": 10, "num2": 5, "operation": "add"}'

# Process data
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello from curl!"}'
```

### Using Postman or Insomnia

Import these endpoints:
- GET: http://localhost:8080/api/health
- GET: http://localhost:8080/api/data
- POST: http://localhost:8080/api/calculate
- POST: http://localhost:8080/api/process

## Next Steps

- Read [README.md](README.md) for architecture details
- Check [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines
- Explore the frontend code in `src/main/resources/webview/`
- Add new API endpoints in `src/main/java/com/example/app/BackendServer.java`

## Getting Help

- Check GitHub Issues
- Review the documentation
- Contact the maintainers

---

**Happy Coding! 🚀**
