package com.example.app;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;

/**
 * WebSocket handler for real-time bidirectional communication
 * Manages WebSocket connections and broadcasts messages
 */
public class WebSocketHandler {
    private static final Map<WsContext, String> sessions = new ConcurrentHashMap<>();
    private static final AtomicInteger nextSessionId = new AtomicInteger(1);
    private static final Gson gson = new Gson();

    public static void handleConnect(WsContext ctx) {
        String sessionId = "session-" + nextSessionId.getAndIncrement();
        sessions.put(ctx, sessionId);
        System.out.println("WebSocket connected: " + sessionId);
        
        // Send welcome message
        ctx.send("{\"type\":\"welcome\",\"sessionId\":\"" + sessionId + "\",\"message\":\"Connected to server\"}");
        
        // Broadcast connection count
        broadcastConnectionCount();
    }

    public static void handleMessage(WsContext ctx, String message) {
        String sessionId = sessions.get(ctx);
        System.out.println("WebSocket message from " + sessionId + ": " + message);
        
        // Echo message back with session info
        ctx.send("{\"type\":\"echo\",\"sessionId\":\"" + sessionId + "\",\"message\":\"" + message + "\"}");
    }

    public static void handleClose(WsContext ctx, int statusCode, String reason) {
        String sessionId = sessions.remove(ctx);
        System.out.println("WebSocket closed: " + sessionId + " (code: " + statusCode + ", reason: " + reason + ")");
        
        // Broadcast updated connection count
        broadcastConnectionCount();
    }

    public static void handleError(WsContext ctx, Throwable throwable) {
        String sessionId = sessions.get(ctx);
        System.err.println("WebSocket error for " + sessionId + ": " + throwable.getMessage());
    }

    private static void broadcastConnectionCount() {
        String message = "{\"type\":\"connectionCount\",\"count\":" + sessions.size() + "}";
        sessions.keySet().forEach(ctx -> {
            try {
                ctx.send(message);
            } catch (Exception e) {
                System.err.println("Failed to broadcast to client: " + e.getMessage());
            }
        });
    }

    public static void broadcast(String message) {
        sessions.keySet().forEach(ctx -> {
            try {
                ctx.send(message);
            } catch (Exception e) {
                System.err.println("Failed to broadcast: " + e.getMessage());
            }
        });
    }

    public static void broadcastTaskUpdate(Map<String, Object> taskData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "task-update");
        message.put("data", taskData);
        message.put("timestamp", System.currentTimeMillis());

        String jsonMessage = gson.toJson(message);
        broadcast(jsonMessage);
    }

    public static int getConnectionCount() {
        return sessions.size();
    }
}
