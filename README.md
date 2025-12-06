# Java WebView Desktop Application

A **native desktop application** with a Java backend and modern web-based UI. This project demonstrates how to build a true desktop application using:

- **Backend**: Java 17 + Javalin (lightweight web framework)
- **Frontend**: HTML5 + CSS3 + JavaScript (vanilla)
- **Desktop UI**: JavaFX WebView (embedded browser)
- **Architecture**: Local web server with native desktop window

## Features

- ✅ **Native Desktop App**: True desktop application with JavaFX WebView
- ✅ **System Tray**: Minimize to system tray with tray icon
- ✅ **Modern UI**: Responsive web interface with gradient design
- ✅ **Menu System**: Native application menus (File, View, Help)
- ✅ **Real-time Communication**: Frontend communicates with Java backend via HTTP/WebSocket
- ✅ **Database Integration**: SQLite database for local data storage and analytics
- ✅ **Data Management**: User data storage, application logs, and API call history
- ✅ **Native Packaging**: Professional installers for Windows, macOS, and Linux
- ✅ **Cross-platform**: Works on Windows, macOS, and Linux
- ✅ **Portable**: Uses relative paths, works with any host address
- ✅ **No external dependencies**: Embedded WebView, no browser required
- ✅ **Easy to extend**: Add new endpoints and UI components easily

## Project Structure

```
java-webview/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── java/com/example/app/
│   │   ├── Application.java         # Browser-based launcher (legacy)
│   │   ├── DesktopApplication.java  # Native JavaFX desktop app
│   │   ├── BackendServer.java       # Javalin web server, API endpoints
│   │   ├── WebSocketHandler.java    # WebSocket connection management
│   │   ├── DatabaseManager.java     # SQLite database management
│   │   ├── FileSystemManager.java   # File system operations
│   │   ├── NotificationManager.java # System notifications
│   │   ├── SettingsManager.java     # Application settings
│   │   ├── UpdateManager.java       # Application updates
│   │   └── TaskManager.java         # Background task management
│   └── resources/webview/
│       ├── index.html               # Main UI structure
│       ├── app.js                   # Frontend logic + WebSocket client
│       └── styles.css               # Modern gradient styling
├── src/assembly/
│   └── dist.xml                     # Maven assembly descriptor
├── package/
│   ├── build.properties             # Build configuration
│   ├── java-webview.desktop         # Linux desktop integration
│   ├── postinstall.sh               # Linux post-install script
│   └── README-icons.md              # Icon creation guide
├── target/
│   └── java-webview-app-1.0.0.jar  # Compiled application (54 MB)
├── build-windows.bat                # Windows MSI builder
├── build-macos.sh                   # macOS DMG builder
├── build-deb.sh                     # Linux DEB builder
├── build-rpm.sh                     # Linux RPM builder
├── run.sh                           # Linux/macOS launcher
├── run.bat                          # Windows launcher
├── PACKAGING.md                     # Native packaging guide
├── README.md                        # Main documentation
├── API.md                           # API documentation
├── SETUP.md                         # Installation & setup guide
├── CONTRIBUTING.md                  # Development guidelines
├── LICENSE                          # MIT License
└── .gitignore                       # Git ignore rules
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **JavaFX** (automatically included via Maven dependencies)
- **System Tray Support** (available on most desktop environments)

## Downloads

Get the latest release from [GitHub Releases](https://github.com/phearun-coder/java-webview/releases):

### Latest Version: 1.3.0

| Platform | Download | Size | Installation |
|----------|----------|------|--------------|
| **Windows** | [java-webview-app-1.3.0.msi](https://github.com/phearun-coder/java-webview/releases/download/v1.3.0/java-webview-app-1.3.0.msi) | ~65 MB | Double-click MSI installer |
| **macOS** | [java-webview-app-1.3.0.dmg](https://github.com/phearun-coder/java-webview/releases/download/v1.3.0/java-webview-app-1.3.0.dmg) | ~58 MB | Mount DMG and drag to Applications |
| **Ubuntu/Debian** | [java-webview-app_1.3.0_amd64.deb](https://github.com/phearun-coder/java-webview/releases/download/v1.3.0/java-webview-app_1.3.0_amd64.deb) | ~55 MB | `sudo apt install ./package.deb` |
| **Red Hat/Fedora** | [java-webview-app-1.3.0-1.x86_64.rpm](https://github.com/phearun-coder/java-webview/releases/download/v1.3.0/java-webview-app-1.3.0-1.x86_64.rpm) | ~55 MB | `sudo dnf install package.rpm` |
| **JAR (Development)** | [java-webview-app-1.3.0.jar](https://github.com/phearun-coder/java-webview/releases/download/v1.3.0/java-webview-app-1.3.0.jar) | 54 MB | `java -jar app.jar` |

> **Note**: Native installers provide the best user experience with system integration, automatic updates, and professional installation.

## Building the Project

```bash
# Build with Maven
mvn clean package

# This creates: target/java-webview-app-1.0.0.jar
```

## Running the Application

### Native Desktop Application (Recommended)

**On Linux/macOS:**
```bash
./run.sh
```

**On Windows:**
```cmd
run.bat
```

This launches a **native desktop application** with:
- Embedded JavaFX WebView (no external browser needed)
- Native window with minimize/maximize/close buttons
- System tray icon for background operation
- Application menu bar (File, View, Help)

### Browser-based Application (Legacy)

```bash
java -jar target/java-webview-app-1.0.0.jar
```

This opens the application in your system web browser.

### Configuration

The application can be configured to run in either desktop or web mode by editing `src/main/resources/config.properties`:

```properties
run.mode=desktop  # or "web"
```

- `desktop`: Launches the native JavaFX desktop application (default)
- `web`: Opens in the system web browser

After changing the config, rebuild with `mvn clean package` and run the JAR.

## Application URLs

Once running, you can access:

- **Main UI**: http://localhost:8080/index.html
- **Health Check**: http://localhost:8080/api/health
- **API Endpoints**: http://localhost:8080/api/*
- **WebSocket**: ws://localhost:8080/ws

## API Endpoints

The backend provides several REST API endpoints:

### `GET /api/health`
Health check endpoint with WebSocket connection count
```json
{
  "status": "ok",
  "timestamp": 1701790123456,
  "wsConnections": 2
}
```

### `GET /api/data`
Get system information
```json
{
  "message": "Hello from Java Backend!",
  "version": "1.0.0",
  "platform": "Linux",
  "javaVersion": "17.0.9",
  "availableProcessors": 8
}
```

### `POST /api/calculate`
Perform calculations
```json
{
  "num1": 10,
  "num2": 5,
  "operation": "add"
}
```

### `POST /api/process`
Process arbitrary data
```json
{
  "message": "Your message",
  "timestamp": 1701790123456
}
```

### `POST /api/broadcast`
Broadcast message to all connected WebSocket clients

### Database Management
- `GET /api/database/stats` - Database statistics and health
- `GET /api/database/userdata` - Get all stored user data
- `POST /api/database/userdata` - Store user data
- `DELETE /api/database/userdata/{key}` - Delete user data
- `GET /api/database/logs` - Application logs history
- `GET /api/database/notifications` - Notification history
- `GET /api/database/api-calls` - API call history
```json
{
  "message": "Hello to all clients!"
}
```

## WebSocket Communication

Real-time bidirectional communication via WebSocket:

- **Endpoint**: `ws://localhost:8080/ws`
- **Auto-reconnection**: 3-second retry on disconnect
- **Connection tracking**: Live connection count display
- **Message types**: welcome, echo, broadcast, connectionCount

See [API.md](API.md) for detailed WebSocket protocol documentation.

## UI Features

The web interface includes:

1. **System Information Card** - Displays OS and system details
2. **Backend Connection Card** - Shows backend health status
3. **Calculator Demo** - Interactive calculator using backend API
4. **Data Exchange Demo** - Send/receive data to/from backend
5. **Native Bridge Demo** - Placeholder for native integration
6. **WebSocket Communication** - Real-time messaging and broadcasting

## Development

### Adding New API Endpoints

Edit `src/main/java/com/example/app/BackendServer.java`:

```java
app.get("/api/myendpoint", ctx -> {
    ctx.json(Map.of("message", "Hello!"));
});
```

### Adding New UI Components

Edit `src/main/resources/webview/index.html` and `app.js`.

### Changing the Port

Modify `BACKEND_PORT` in `Application.java` (default: 8080).

## Technologies Used

- **[JavaFX 21](https://openjfx.io/)** - Native desktop UI framework with WebView
- **[Javalin 5.6](https://javalin.io/)** - Lightweight Java web framework
- **[Gson 2.10](https://github.com/google/gson)** - JSON serialization/deserialization
- **[SLF4J](http://www.slf4j.org/)** - Logging facade
- **[Jetty](https://www.eclipse.org/jetty/)** - Embedded web server
- **WebSocket** - Real-time bidirectional communication

## Future Enhancements

To further enhance this desktop application:

1. **Add JavaFX WebView** - For native window with embedded browser
2. **Add Electron-like packaging** - Using jpackage (Java 14+)
3. **Add native system tray** - For background operation
4. **Add file system access** - For local file operations
5. **Database integration** - SQLite or H2 for local storage

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

---

**Made with ❤️ using Java + Web Technologies**