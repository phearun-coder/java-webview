# API Documentation

This document describes the REST API endpoints provided by the Java WebView Desktop Application backend.

## Base URL

```
http://localhost:8080
```

## Endpoints

---

### Health Check

Check if the backend server is running and healthy.

**Endpoint:** `GET /api/health`

**Response:**
```json
{
  "status": "ok",
  "timestamp": 1701790123456
}
```

**Example:**
```bash
curl http://localhost:8080/api/health
```

**Response Codes:**
- `200 OK` - Server is healthy

---

### Get System Data

Get system information and a greeting message from the backend.

**Endpoint:** `GET /api/data`

**Response:**
```json
{
  "message": "Hello from Java Backend!",
  "version": "1.0.0",
  "platform": "Linux"
}
```

**Response Fields:**
- `message` (string) - Greeting message
- `version` (string) - Application version
- `platform` (string) - Operating system name

**Example:**
```bash
curl http://localhost:8080/api/data
```

**Response Codes:**
- `200 OK` - Success

---

### Calculate

Perform arithmetic operations on two numbers.

**Endpoint:** `POST /api/calculate`

**Request Body:**
```json
{
  "num1": 10,
  "num2": 5,
  "operation": "add"
}
```

**Request Fields:**
- `num1` (number, required) - First operand
- `num2` (number, required) - Second operand
- `operation` (string, required) - Operation to perform
  - Valid values: `"add"`, `"subtract"`, `"multiply"`, `"divide"`

**Response (Success):**
```json
{
  "result": 15,
  "operation": "add"
}
```

**Response Fields:**
- `result` (number) - Calculation result
- `operation` (string) - Operation performed

**Response (Error):**
```json
{
  "error": "Invalid operation"
}
```

**Examples:**

Addition:
```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1": 10, "num2": 5, "operation": "add"}'
```

Division:
```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1": 20, "num2": 4, "operation": "divide"}'
```

**Response Codes:**
- `200 OK` - Calculation successful
- `400 Bad Request` - Invalid input or operation

**Error Cases:**
- Missing required fields
- Invalid operation type
- Division by zero
- Non-numeric values

---

### Process Data

Process arbitrary data and echo it back with metadata.

**Endpoint:** `POST /api/process`

**Request Body:**
```json
{
  "message": "Hello from frontend",
  "timestamp": 1701790123456,
  "additionalData": {
    "key": "value"
  }
}
```

**Request Fields:**
- Any valid JSON object

**Response:**
```json
{
  "received": {
    "message": "Hello from frontend",
    "timestamp": 1701790123456,
    "additionalData": {
      "key": "value"
    }
  },
  "processed": true,
  "timestamp": 1701790123999
}
```

**Response Fields:**
- `received` (object) - Echo of the request body
- `processed` (boolean) - Processing status
- `timestamp` (number) - Server processing timestamp

**Example:**
```bash
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Test message",
    "data": [1, 2, 3],
    "metadata": {
      "source": "curl"
    }
  }'
```

**Response Codes:**
- `200 OK` - Data processed successfully

---

## Database Management

The application includes SQLite database integration for local data storage and application analytics.

### Get Database Statistics

Get comprehensive statistics about the SQLite database.

**Endpoint:** `GET /api/database/stats`

**Response:**
```json
{
  "healthy": true,
  "database_path": "/home/user/.java-webview-app/java-webview.db",
  "database_size_kb": 45.2,
  "app_logs_count": 1250,
  "user_data_count": 15,
  "websocket_messages_count": 89,
  "api_calls_count": 2340,
  "notifications_count": 45,
  "file_operations_count": 23
}
```

**Response Fields:**
- `healthy` (boolean) - Database health status
- `database_path` (string) - Full path to database file
- `database_size_kb` (number) - Database file size in KB
- `*_count` (number) - Record counts for each table

**Example:**
```bash
curl http://localhost:8080/api/database/stats
```

**Response Codes:**
- `200 OK` - Statistics retrieved successfully

---

### Get User Data

Retrieve all user data stored in the database.

**Endpoint:** `GET /api/database/userdata`

**Response:**
```json
{
  "user.name": {
    "value": "John Doe",
    "data_type": "string",
    "created_at": "2024-12-06 10:30:15",
    "updated_at": "2024-12-06 10:30:15"
  },
  "app.theme": {
    "value": "dark",
    "data_type": "string",
    "created_at": "2024-12-06 10:25:00",
    "updated_at": "2024-12-06 10:35:22"
  }
}
```

**Response Fields:**
- Key-value pairs where each value contains:
  - `value` - The stored value (converted to appropriate type)
  - `data_type` (string) - Data type: "string", "integer", "boolean", "float"
  - `created_at` (string) - ISO timestamp of creation
  - `updated_at` (string) - ISO timestamp of last update

**Example:**
```bash
curl http://localhost:8080/api/database/userdata
```

**Response Codes:**
- `200 OK` - User data retrieved successfully

---

### Set User Data

Store or update user data in the database.

**Endpoint:** `POST /api/database/userdata`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "key": "user.name",
  "value": "John Doe",
  "dataType": "string"
}
```

**Request Fields:**
- `key` (string, required) - Unique key for the data
- `value` (any, required) - Value to store
- `dataType` (string, optional) - Data type: "string", "integer", "boolean", "float". Defaults to "string"

**Response:**
```json
{
  "success": true
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/database/userdata \
  -H "Content-Type: application/json" \
  -d '{
    "key": "user.email",
    "value": "john@example.com",
    "dataType": "string"
  }'
```

**Response Codes:**
- `200 OK` - Data stored successfully
- `400 Bad Request` - Missing required fields

---

### Delete User Data

Remove user data from the database.

**Endpoint:** `DELETE /api/database/userdata/{key}`

**Path Parameters:**
- `key` (string) - The key to delete

**Response:**
```json
{
  "deleted": true
}
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/database/userdata/user.name
```

**Response Codes:**
- `200 OK` - Data deleted successfully (or key didn't exist)

---

### Get Application Logs

Retrieve recent application logs from the database.

**Endpoint:** `GET /api/database/logs`

**Query Parameters:**
- `limit` (number, optional) - Maximum number of logs to return. Defaults to 100.

**Response:**
```json
[
  {
    "timestamp": "2024-12-06 10:30:15",
    "level": "INFO",
    "category": "Application",
    "message": "Database initialized successfully",
    "details": null
  },
  {
    "timestamp": "2024-12-06 10:30:16",
    "level": "ERROR",
    "category": "WebSocket",
    "message": "Connection failed",
    "details": "java.net.ConnectException: Connection refused"
  }
]
```

**Response Fields (Array):**
- `timestamp` (string) - ISO timestamp
- `level` (string) - Log level: "INFO", "WARN", "ERROR"
- `category` (string) - Log category
- `message` (string) - Log message
- `details` (string) - Additional details (may be null)

**Example:**
```bash
curl "http://localhost:8080/api/database/logs?limit=50"
```

**Response Codes:**
- `200 OK` - Logs retrieved successfully

---

### Get Notification History

Retrieve recent notification history from the database.

**Endpoint:** `GET /api/database/notifications`

**Query Parameters:**
- `limit` (number, optional) - Maximum number of notifications to return. Defaults to 50.

**Response:**
```json
[
  {
    "type": "success",
    "message": "File uploaded successfully",
    "success": true,
    "timestamp": "2024-12-06 10:30:15"
  },
  {
    "type": "error",
    "message": "Failed to connect to server",
    "success": false,
    "timestamp": "2024-12-06 10:25:30"
  }
]
```

**Response Fields (Array):**
- `type` (string) - Notification type
- `message` (string) - Notification message
- `success` (boolean) - Whether the operation was successful
- `timestamp` (string) - ISO timestamp

**Example:**
```bash
curl "http://localhost:8080/api/database/notifications?limit=25"
```

**Response Codes:**
- `200 OK` - Notifications retrieved successfully

---

### Get API Call History

Retrieve recent API call history from the database.

**Endpoint:** `GET /api/database/api-calls`

**Query Parameters:**
- `limit` (number, optional) - Maximum number of API calls to return. Defaults to 100.

**Response:**
```json
[
  {
    "method": "GET",
    "endpoint": "/api/health",
    "status_code": 200,
    "response_time": 5,
    "success": true,
    "timestamp": "2024-12-06 10:30:15"
  },
  {
    "method": "POST",
    "endpoint": "/api/process",
    "status_code": 500,
    "response_time": 150,
    "success": false,
    "timestamp": "2024-12-06 10:29:45"
  }
]
```

**Response Fields (Array):**
- `method` (string) - HTTP method
- `endpoint` (string) - API endpoint
- `status_code` (number) - HTTP status code
- `response_time` (number) - Response time in milliseconds
- `success` (boolean) - Whether the call was successful
- `timestamp` (string) - ISO timestamp

**Example:**
```bash
curl "http://localhost:8080/api/database/api-calls?limit=100"
```

**Response Codes:**
- `200 OK` - API calls retrieved successfully

---

## Static Files

The application also serves static files for the web UI.

### Main UI

**Endpoint:** `GET /index.html` or `GET /`

Serves the main web interface.

### JavaScript

**Endpoint:** `GET /app.js`

Serves the frontend JavaScript logic.

### Stylesheet

**Endpoint:** `GET /styles.css`

Serves the frontend CSS styles.

---

## Error Handling

All endpoints return JSON error responses in the following format:

```json
{
  "error": "Error message description"
}
```

### Common Error Codes:

- `400 Bad Request` - Invalid input or malformed request
- `404 Not Found` - Endpoint does not exist
- `500 Internal Server Error` - Server-side error

---

## CORS

CORS (Cross-Origin Resource Sharing) is enabled for all origins. This allows the web UI to communicate with the backend from any domain.

---

## Authentication

Currently, no authentication is required for API endpoints. This is suitable for local desktop applications. For production deployments with network access, implement proper authentication and authorization.

---

## Rate Limiting

No rate limiting is currently implemented. All requests are processed immediately.

---

## API Client Examples

### JavaScript (Fetch API)

```javascript
// GET request
async function getData() {
    const response = await fetch('http://localhost:8080/api/data');
    const data = await response.json();
    console.log(data);
}

// POST request
async function calculate(num1, num2, operation) {
    const response = await fetch('http://localhost:8080/api/calculate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ num1, num2, operation })
    });
    const result = await response.json();
    return result;
}
```

### Python (requests)

```python
import requests

# GET request
response = requests.get('http://localhost:8080/api/data')
data = response.json()
print(data)

# POST request
payload = {
    'num1': 10,
    'num2': 5,
    'operation': 'add'
}
response = requests.post(
    'http://localhost:8080/api/calculate',
    json=payload
)
result = response.json()
print(result)
```

### Java (HttpClient)

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

HttpClient client = HttpClient.newHttpClient();

// GET request
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/data"))
    .GET()
    .build();

HttpResponse<String> response = client.send(
    request,
    HttpResponse.BodyHandlers.ofString()
);
System.out.println(response.body());

// POST request
String json = """
    {
        "num1": 10,
        "num2": 5,
        "operation": "add"
    }
    """;

HttpRequest postRequest = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/calculate"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(json))
    .build();

HttpResponse<String> postResponse = client.send(
    postRequest,
    HttpResponse.BodyHandlers.ofString()
);
System.out.println(postResponse.body());
```

---

## WebSocket Communication

Real-time bidirectional communication using WebSocket protocol.

### Connect to WebSocket

**Endpoint:** `WS /ws`

**Connection URL:**
```
ws://localhost:8080/ws
```

**Connection Example (JavaScript):**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws');

ws.onopen = () => {
    console.log('WebSocket connected');
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('Received:', data);
};

ws.onclose = () => {
    console.log('WebSocket disconnected');
};

ws.onerror = (error) => {
    console.error('WebSocket error:', error);
};
```

**Server Messages:**

1. **Welcome Message** (sent on connection):
```json
{
  "type": "welcome",
  "sessionId": "session-1",
  "message": "Connected to server"
}
```

2. **Connection Count Update** (sent on connect/disconnect):
```json
{
  "type": "connectionCount",
  "count": 3
}
```

3. **Echo Response** (sent when client sends message):
```json
{
  "type": "echo",
  "sessionId": "session-1",
  "message": "your message"
}
```

4. **Broadcast Message** (from REST API):
```json
{
  "type": "broadcast",
  "message": "broadcast text"
}
```

**Sending Messages:**
```javascript
// Send a text message
ws.send('Hello from client!');

// Server echoes back with session info
```

---

### Broadcast to All Clients

Broadcast a message to all connected WebSocket clients via REST API.

**Endpoint:** `POST /api/broadcast`

**Request Body:**
```json
{
  "message": "Hello to all clients!"
}
```

**Request Fields:**
- `message` (string, required) - Message to broadcast

**Response:**
```json
{
  "status": "success",
  "recipients": 3,
  "message": "Hello to all clients!"
}
```

**Response Fields:**
- `status` (string) - Operation status
- `recipients` (number) - Number of clients who received the message
- `message` (string) - The broadcasted message

**Example:**
```bash
curl -X POST http://localhost:8080/api/broadcast \
  -H "Content-Type: application/json" \
  -d '{"message":"System announcement"}'
```

**Response Codes:**
- `200 OK` - Message broadcasted successfully
- `400 Bad Request` - Missing or invalid message

**WebSocket Clients Receive:**
```json
{
  "type": "broadcast",
  "message": "System announcement"
}
```

---

## Extending the API

To add new endpoints, edit `src/main/java/com/example/app/BackendServer.java`:

```java
// Add in setupRoutes() method
app.get("/api/myendpoint", ctx -> {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Hello!");
    ctx.json(response);
});

app.post("/api/myendpoint", ctx -> {
    String requestBody = ctx.body();
    // Process request
    ctx.json(Map.of("status", "success"));
});
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details on development.

---

## Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Check existing documentation
- Contact the maintainers

---

**Last Updated:** December 5, 2025
