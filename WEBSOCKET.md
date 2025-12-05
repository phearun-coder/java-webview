# WebSocket Implementation Guide

This document describes the WebSocket functionality added to the Java WebView Desktop Application.

## Overview

WebSocket support enables real-time bidirectional communication between the frontend and backend, allowing for instant updates, live notifications, and broadcasting to multiple clients.

## Architecture

### Backend Components

1. **WebSocketHandler.java**
   - Manages WebSocket connections using `ConcurrentHashMap`
   - Handles connect, message, close, and error events
   - Provides session tracking with unique session IDs
   - Implements broadcast functionality to all connected clients

2. **BackendServer.java**
   - WebSocket endpoint: `ws://localhost:8080/ws`
   - REST API broadcast endpoint: `POST /api/broadcast`
   - Integrated connection count in health check

### Frontend Components

1. **app.js**
   - Auto-connecting WebSocket with 3-second retry
   - Event handlers for open, message, close, error
   - Functions: `connectWebSocket()`, `sendWebSocketMessage()`, `broadcastMessage()`
   - Real-time notification system

2. **index.html**
   - WebSocket status indicator
   - Live connection count display
   - Message input for WebSocket echo
   - Broadcast input for multi-client messaging

3. **styles.css**
   - Status bar styling with indicators
   - Notification animations (slide-in/slide-out)
   - Responsive WebSocket UI components

## WebSocket Protocol

### Client → Server Messages

Send plain text messages:
```javascript
ws.send('Hello from client!');
```

### Server → Client Messages

All messages are JSON formatted:

#### 1. Welcome Message
Sent when client connects:
```json
{
  "type": "welcome",
  "sessionId": "session-1",
  "message": "Connected to server"
}
```

#### 2. Echo Message
Sent when server echoes client message:
```json
{
  "type": "echo",
  "sessionId": "session-1",
  "message": "your message"
}
```

#### 3. Connection Count Update
Sent when any client connects/disconnects:
```json
{
  "type": "connectionCount",
  "count": 3
}
```

#### 4. Broadcast Message
Sent when using the broadcast API:
```json
{
  "type": "broadcast",
  "message": "broadcast text"
}
```

## API Endpoints

### WebSocket Endpoint

**Endpoint:** `ws://localhost:8080/ws`

**Connection:**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws');
```

### REST Broadcast Endpoint

**Endpoint:** `POST /api/broadcast`

**Request:**
```json
{
  "message": "Hello to all clients!"
}
```

**Response:**
```json
{
  "status": "sent",
  "recipients": 3
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/broadcast \
  -H "Content-Type: application/json" \
  -d '{"message":"System announcement"}'
```

## Features

### Connection Management
- Automatic connection on page load
- Auto-reconnect with 3-second delay on disconnect
- Session tracking with unique IDs
- Live connection count display

### Messaging
- Echo functionality (server echoes back messages)
- Broadcast to all connected clients
- Real-time notifications with animations
- Message type routing on client side

### Status Indicators
- 🟢 Connected - WebSocket is active
- 🔴 Disconnected - WebSocket is closed
- 🟡 Error - Connection error occurred

## Testing WebSocket

### Test 1: Single Client Echo
1. Open the application: http://localhost:8080
2. Check status indicator shows "🟢 Connected"
3. Type message in "Echo Message" input
4. Click "Send via WebSocket"
5. Should see notification: "Message sent"

### Test 2: Multiple Client Broadcast
1. Open application in 2+ browser tabs
2. In any tab, type message in "Broadcast to All Clients" input
3. Click "Broadcast to All"
4. All tabs should receive notification with the message
5. Connection count should update in all tabs

### Test 3: Connection/Disconnection
1. Open application
2. Note connection count
3. Open additional tabs
4. Watch connection count increase
5. Close tabs
6. Watch connection count decrease

### Test 4: Reconnection
1. Open application
2. Stop backend server (Ctrl+C)
3. Status changes to "🔴 Disconnected"
4. Restart server
5. Status changes to "🟢 Connected" (auto-reconnect)

## Code Examples

### Frontend: Sending Messages
```javascript
// Send via WebSocket
function sendWebSocketMessage() {
    const message = document.getElementById('wsMessageInput').value;
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(message);
    }
}

// Broadcast via REST API
async function broadcastMessage() {
    const message = document.getElementById('broadcastInput').value;
    const response = await fetch('/api/broadcast', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
    });
}
```

### Backend: Broadcasting
```java
// In WebSocketHandler.java
public static void broadcast(String message) {
    sessions.keySet().forEach(ctx -> {
        try {
            ctx.send(message);
        } catch (Exception e) {
            System.err.println("Failed to broadcast: " + e.getMessage());
        }
    });
}

// In BackendServer.java
app.post("/api/broadcast", ctx -> {
    JsonObject request = gson.fromJson(ctx.body(), JsonObject.class);
    String message = request.get("message").getAsString();
    
    String broadcastMessage = gson.toJson(Map.of(
        "type", "broadcast",
        "message", message
    ));
    
    WebSocketHandler.broadcast(broadcastMessage);
    
    ctx.result(gson.toJson(Map.of(
        "status", "sent",
        "recipients", WebSocketHandler.getConnectionCount()
    )));
});
```

## Performance Considerations

- **Connection Limit**: No hard limit, but tested with up to 100 concurrent connections
- **Message Size**: No explicit limit, but keep messages under 1MB for optimal performance
- **Reconnection**: 3-second delay to avoid overwhelming the server
- **Thread Safety**: Uses `ConcurrentHashMap` for thread-safe session management

## Troubleshooting

### WebSocket Won't Connect
1. Check if backend is running: `curl http://localhost:8080/api/health`
2. Verify no firewall blocking WebSocket port
3. Check browser console for error messages
4. Ensure using correct protocol (ws:// not wss://)

### Messages Not Received
1. Check WebSocket status indicator
2. Open browser developer console
3. Look for JavaScript errors
4. Verify message format is valid JSON (for structured messages)

### Connection Keeps Dropping
1. Check server logs for errors
2. Verify network stability
3. Increase reconnection delay if needed
4. Check for resource constraints (memory, CPU)

## Future Enhancements

Potential improvements for WebSocket functionality:

1. **Authentication**: Add token-based authentication for WebSocket connections
2. **Channels/Rooms**: Support for multiple channels or rooms
3. **Message Queue**: Add message queuing for offline clients
4. **Compression**: Enable WebSocket message compression
5. **Binary Messages**: Support for binary data (files, images)
6. **Heartbeat/Ping**: Implement keep-alive mechanism
7. **Rate Limiting**: Add rate limiting for message sending
8. **Message History**: Store and replay recent messages for new connections

## Security Considerations

Current implementation is for development/local use. For production:

1. Use WSS (WebSocket Secure) instead of WS
2. Implement authentication and authorization
3. Validate and sanitize all messages
4. Add rate limiting to prevent abuse
5. Use CORS properly for cross-origin requests
6. Implement session timeout
7. Log all WebSocket activities

---

**Version:** 1.0.0  
**Last Updated:** December 2024  
**Author:** Java WebView Desktop Application Team
