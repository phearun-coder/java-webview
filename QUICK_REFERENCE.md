# Quick Reference Card

## 🚀 Quick Start

```bash
# Build and run
./run.sh              # Linux/macOS
run.bat               # Windows

# Or manually
mvn clean package
java -jar target/java-webview-app-1.0.0.jar
```

## 🌐 Access Points

- **Main UI:** http://localhost:8080
- **API Base:** http://localhost:8080/api
- **Health Check:** http://localhost:8080/api/health

## 📡 API Quick Reference

### GET Endpoints

```bash
# Health check
curl http://localhost:8080/api/health

# Get data
curl http://localhost:8080/api/data
```

### POST Endpoints

```bash
# Calculate
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1":10,"num2":5,"operation":"add"}'

# Process data
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{"message":"hello"}'
```

## 📁 File Locations

| File | Location |
|------|----------|
| Entry Point | `src/main/java/com/example/app/Application.java` |
| API Server | `src/main/java/com/example/app/BackendServer.java` |
| UI HTML | `src/main/resources/webview/index.html` |
| UI JS | `src/main/resources/webview/app.js` |
| UI CSS | `src/main/resources/webview/styles.css` |
| JAR Output | `target/java-webview-app-1.0.0.jar` |

## ⚙️ Configuration

### Change Port

Edit `Application.java`:
```java
private static final int BACKEND_PORT = 9090; // Change port
```

### Add API Endpoint

Edit `BackendServer.java`:
```java
app.get("/api/myendpoint", ctx -> {
    ctx.json(Map.of("status", "ok"));
});
```

### Add UI Component

Edit `index.html`:
```html
<div class="card">
    <h2>My Feature</h2>
    <button onclick="myFunction()">Click</button>
</div>
```

Edit `app.js`:
```javascript
async function myFunction() {
    const data = await fetch(`${API_BASE}/myendpoint`);
    console.log(await data.json());
}
```

## 🛠️ Development Commands

```bash
# Clean build
mvn clean

# Compile only
mvn compile

# Package
mvn package

# Run with Maven
mvn exec:java -Dexec.mainClass="com.example.app.Application"

# Clean Maven cache
rm -rf ~/.m2/repository
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Linux/macOS: Find and kill process
lsof -i :8080
kill -9 <PID>

# Windows: Find and kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Build Fails
```bash
# Clean and rebuild
mvn clean
mvn package -U
```

### Browser Won't Open
- Normal in containers
- Manually open: http://localhost:8080

## 📊 Key Metrics

| Metric | Value |
|--------|-------|
| Startup Time | ~1 second |
| Memory Usage | ~150 MB |
| JAR Size | 8.4 MB |
| API Response | < 10ms |

## 🗂️ Project Structure

```
java-webview/
├── src/main/
│   ├── java/...       # Backend code
│   └── resources/...  # Frontend files
├── target/            # Build output
├── pom.xml           # Maven config
├── run.sh/.bat       # Launchers
└── *.md              # Documentation
```

## 📚 Documentation

- `README.md` - Main documentation
- `SETUP.md` - Installation guide
- `API.md` - API reference
- `CONTRIBUTING.md` - Development guide
- `PROJECT_STATUS.md` - Architecture overview

## 🔗 Useful Links

- **Javalin Docs:** https://javalin.io/documentation
- **Maven Docs:** https://maven.apache.org/guides/
- **Java Download:** https://adoptium.net/

## 💡 Tips

- Frontend changes: Just refresh browser
- Backend changes: Rebuild with `mvn package`
- Check logs: Terminal output
- API testing: Use curl or Postman
- Port conflict: Change BACKEND_PORT

## 🎯 Common Tasks

### Add New Feature
1. Add API endpoint in `BackendServer.java`
2. Add UI component in `index.html`
3. Add logic in `app.js`
4. Style in `styles.css`
5. Rebuild: `mvn package`

### Deploy
1. Build: `mvn clean package`
2. Copy: `target/java-webview-app-1.0.0.jar`
3. Run: `java -jar java-webview-app-1.0.0.jar`

### Test API
```bash
# Health
curl http://localhost:8080/api/health

# Calculate
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1":15,"num2":3,"operation":"multiply"}'
```

## 📞 Getting Help

1. Check documentation
2. Review code comments
3. Test with curl
4. Check server logs
5. Open GitHub issue

---

**Version:** 1.0.0 | **Last Updated:** Dec 5, 2025
