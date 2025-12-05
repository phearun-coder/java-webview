# Contributing to Java WebView Desktop App

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Getting Started

1. **Fork the repository**
   ```bash
   git clone https://github.com/yourusername/java-webview.git
   cd java-webview
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   java -jar target/java-webview-app-1.0.0.jar
   ```

## Project Structure

```
java-webview/
├── src/main/
│   ├── java/com/example/app/
│   │   ├── Application.java         # Entry point
│   │   └── BackendServer.java       # API server
│   └── resources/webview/
│       ├── index.html               # Main UI
│       ├── app.js                   # Frontend logic
│       └── styles.css               # Styling
├── pom.xml                          # Maven config
├── README.md                        # Documentation
├── SETUP.md                         # Setup guide
└── CONTRIBUTING.md                  # This file
```

## Development Workflow

### Backend Changes (Java)

1. Edit files in `src/main/java/com/example/app/`
2. Rebuild and run:
   ```bash
   mvn clean package
   java -jar target/java-webview-app-1.0.0.jar
   ```

### Frontend Changes (HTML/CSS/JS)

1. Edit files in `src/main/resources/webview/`
2. Rebuild:
   ```bash
   mvn clean package
   ```
3. Refresh your browser (Ctrl+R / Cmd+R)

## Coding Standards

### Java Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Add JavaDoc comments for public methods
- Keep methods focused and under 50 lines when possible

**Example:**
```java
/**
 * Processes user input and returns result
 * @param input The user input string
 * @return Processed result
 */
public String processInput(String input) {
    // Implementation
    return result;
}
```

### JavaScript Code Style

- Use 4 spaces for indentation
- Use `const` and `let`, avoid `var`
- Use async/await for promises
- Add JSDoc comments for functions

**Example:**
```javascript
/**
 * Fetches data from the API
 * @param {string} endpoint - The API endpoint
 * @returns {Promise<Object>} The response data
 */
async function fetchData(endpoint) {
    const response = await fetch(endpoint);
    return response.json();
}
```

### CSS Code Style

- Use 4 spaces for indentation
- Follow BEM naming convention when appropriate
- Group related properties together
- Add comments for complex styles

## Adding New Features

### Adding a New API Endpoint

1. **Edit BackendServer.java:**
   ```java
   app.get("/api/mynewfeature", ctx -> {
       Map<String, Object> response = new HashMap<>();
       response.put("status", "success");
       response.put("data", "your data here");
       ctx.json(response);
   });
   ```

2. **Add frontend function in app.js:**
   ```javascript
   async function getNewFeature() {
       const response = await fetch(`${API_BASE}/mynewfeature`);
       return response.json();
   }
   ```

3. **Update the UI in index.html** to display the new feature

### Adding a New UI Component

1. **Add HTML structure** in `index.html`:
   ```html
   <div class="card">
       <h2>My New Feature</h2>
       <div id="myFeatureContent"></div>
       <button onclick="loadMyFeature()">Load Feature</button>
   </div>
   ```

2. **Add styling** in `styles.css`:
   ```css
   .my-feature-class {
       /* Your styles */
   }
   ```

3. **Add logic** in `app.js`:
   ```javascript
   async function loadMyFeature() {
       const data = await getNewFeature();
       document.getElementById('myFeatureContent').innerHTML = 
           `<p>${data.status}</p>`;
   }
   ```

## Testing

### Manual Testing

1. Build and run the application
2. Test all UI components work correctly
3. Test all API endpoints return expected data
4. Test on different browsers (Chrome, Firefox, Edge)
5. Test error handling (network failures, invalid input)

### API Testing with curl

```bash
# Test GET endpoint
curl http://localhost:8080/api/data

# Test POST endpoint
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{"message": "test"}'
```

## Submitting Changes

### Commit Guidelines

- Use clear, descriptive commit messages
- Start with a verb: "Add", "Fix", "Update", "Remove"
- Reference issues when applicable

**Examples:**
```
Add calculator API endpoint
Fix browser auto-launch on Windows
Update README with deployment instructions
Remove deprecated dependency
```

### Pull Request Process

1. **Create a branch:**
   ```bash
   git checkout -b feature/my-new-feature
   ```

2. **Make your changes and commit:**
   ```bash
   git add .
   git commit -m "Add my new feature"
   ```

3. **Push to your fork:**
   ```bash
   git push origin feature/my-new-feature
   ```

4. **Create a Pull Request:**
   - Go to GitHub
   - Click "New Pull Request"
   - Describe your changes
   - Reference any related issues

5. **PR Checklist:**
   - [ ] Code builds successfully
   - [ ] All features work as expected
   - [ ] Code follows project style guidelines
   - [ ] Comments and documentation updated
   - [ ] No unnecessary files included

## Common Issues

### Build Fails

```bash
# Clean and rebuild
mvn clean
rm -rf target
mvn package
```

### Port Already in Use

Change `BACKEND_PORT` in `Application.java` to a different port.

### Dependency Issues

```bash
# Clear Maven cache
rm -rf ~/.m2/repository
mvn clean package
```

## Code Review

All submissions require review. We use GitHub pull requests for this purpose. Reviewers will check:

- Code quality and style
- Feature completeness
- Documentation updates
- Testing coverage
- No breaking changes

## Documentation

When adding new features, update:

- `README.md` - Overall documentation
- `SETUP.md` - Setup instructions
- Code comments - Inline documentation
- API documentation - If adding endpoints

## Community

- Be respectful and constructive
- Ask questions if something is unclear
- Help others when you can
- Share your knowledge

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

## Questions?

- Open an issue for bugs or feature requests
- Start a discussion for general questions
- Contact the maintainers for sensitive topics

---

**Thank you for contributing! 🎉**
