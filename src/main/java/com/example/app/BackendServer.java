package com.example.app;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Backend server using Javalin
 * Provides REST APIs for the WebView frontend
 */
public class BackendServer {
    private final Javalin app;
    private final Gson gson;
    private final int port;
    private final FileSystemManager fileSystemManager;
    private final NotificationManager notificationManager;
    private final SettingsManager settingsManager;
    private final UpdateManager updateManager;
    private final TaskManager taskManager;

    public BackendServer(int port) {
        this.port = port;
        this.app = Javalin.create(config -> {
            // Serve static files from resources
            config.staticFiles.add("/webview", Location.CLASSPATH);
            // Enable CORS for WebView
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });
        this.gson = new Gson();
        this.fileSystemManager = FileSystemManager.getInstance();
        this.notificationManager = NotificationManager.getInstance();
        this.settingsManager = SettingsManager.getInstance();
        this.updateManager = UpdateManager.getInstance();
        this.taskManager = TaskManager.getInstance();
        setupRoutes();
    }

    private void setupRoutes() {
        // WebSocket endpoint for real-time communication
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> WebSocketHandler.handleConnect(ctx));
            ws.onMessage(ctx -> WebSocketHandler.handleMessage(ctx, ctx.message()));
            ws.onClose(ctx -> WebSocketHandler.handleClose(ctx, ctx.status(), ctx.reason()));
            ws.onError(ctx -> WebSocketHandler.handleError(ctx, ctx.error()));
        });

        // Health check endpoint
        app.get("/api/health", ctx -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("timestamp", System.currentTimeMillis());
            response.put("wsConnections", WebSocketHandler.getConnectionCount());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(response));
        });

        // Example GET endpoint
        app.get("/api/data", ctx -> {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Hello from Java Backend!");
            data.put("version", "1.0.0");
            data.put("platform", System.getProperty("os.name"));
            data.put("javaVersion", System.getProperty("java.version"));
            data.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(data));
        });

        // Example POST endpoint
        app.post("/api/process", ctx -> {
            String requestBody = ctx.body();
            Map<String, Object> request = gson.fromJson(requestBody, Map.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("received", request);
            response.put("processed", true);
            response.put("timestamp", System.currentTimeMillis());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(response));
        });

        // Example calculation endpoint
        app.post("/api/calculate", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            
            try {
                double num1 = ((Number) request.get("num1")).doubleValue();
                double num2 = ((Number) request.get("num2")).doubleValue();
                String operation = (String) request.getOrDefault("operation", "add");
                
                double result;
                switch (operation) {
                    case "add": result = num1 + num2; break;
                    case "subtract": result = num1 - num2; break;
                    case "multiply": result = num1 * num2; break;
                    case "divide": result = num1 / num2; break;
                    default: throw new IllegalArgumentException("Invalid operation");
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("result", result);
                response.put("operation", operation);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(response));
            } catch (Exception e) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", e.getMessage())));
            }
        });

        // Broadcast endpoint - sends message to all WebSocket clients
        app.post("/api/broadcast", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String message = (String) request.get("message");
            
            if (message != null && !message.isEmpty()) {
                WebSocketHandler.broadcast("{\"type\":\"broadcast\",\"message\":\"" + message + "\"}");
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sent");
                response.put("recipients", WebSocketHandler.getConnectionCount());
                ctx.contentType("application/json");
                ctx.result(gson.toJson(response));
            } else {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Message is required")));
            }
        });

        // File system endpoints
        app.get("/api/files/info", ctx -> {
            String path = ctx.queryParam("path");
            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileInfo info = fileSystemManager.getFileInfo(path);
            if (info == null) {
                ctx.status(404);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "File not found")));
                return;
            }

            ctx.contentType("application/json");
            ctx.result(gson.toJson(info));
        });

        app.get("/api/files/list", ctx -> {
            String path = ctx.queryParam("path");
            if (path == null || path.isEmpty()) {
                path = fileSystemManager.getCurrentDirectory();
            }

            FileSystemManager.DirectoryListing listing = fileSystemManager.listDirectory(path);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(listing));
        });

        app.get("/api/files/read", ctx -> {
            String path = ctx.queryParam("path");
            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.readTextFile(path);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.post("/api/files/write", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String path = (String) request.get("path");
            String content = (String) request.get("content");

            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.writeTextFile(path, content);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.post("/api/files/append", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String path = (String) request.get("path");
            String content = (String) request.get("content");

            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.appendTextFile(path, content);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.delete("/api/files/delete", ctx -> {
            String path = ctx.queryParam("path");
            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.delete(path);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.post("/api/files/copy", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String source = (String) request.get("source");
            String dest = (String) request.get("dest");

            if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Source and dest parameters are required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.copy(source, dest);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.post("/api/files/move", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String source = (String) request.get("source");
            String dest = (String) request.get("dest");

            if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Source and dest parameters are required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.move(source, dest);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.post("/api/files/mkdir", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String path = (String) request.get("path");

            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            FileSystemManager.FileOperationResult result = fileSystemManager.createDirectory(path);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        app.get("/api/files/exists", ctx -> {
            String path = ctx.queryParam("path");
            if (path == null || path.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Path parameter is required")));
                return;
            }

            boolean exists = fileSystemManager.exists(path);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("exists", exists)));
        });

        // Notification endpoints
        app.post("/api/notifications/show", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String type = (String) request.getOrDefault("type", "info");

            if (title == null || title.isEmpty() || message == null || message.isEmpty()) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Title and message are required")));
                return;
            }

            boolean success = false;
            switch (type.toLowerCase()) {
                case "info":
                    success = notificationManager.showInfo(title, message);
                    break;
                case "warning":
                    success = notificationManager.showWarning(title, message);
                    break;
                case "error":
                    success = notificationManager.showError(title, message);
                    break;
                case "success":
                    success = notificationManager.showSuccess(title, message);
                    break;
                default:
                    success = notificationManager.showInfo(title, message);
                    break;
            }

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("success", success, "type", type)));
        });

        app.get("/api/notifications/status", ctx -> {
            Map<String, Object> status = new HashMap<>();
            status.put("supported", notificationManager.isSupported());
            status.put("initialized", notificationManager.isInitialized());
            status.put("availableTypes", java.util.Arrays.asList("info", "warning", "error", "success"));

            ctx.contentType("application/json");
            ctx.result(gson.toJson(status));
        });

        app.post("/api/notifications/file-operation", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String operation = (String) request.get("operation");
            String filePath = (String) request.get("filePath");
            Boolean success = (Boolean) request.get("success");

            if (operation == null || filePath == null || success == null) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Operation, filePath, and success are required")));
                return;
            }

            notificationManager.showFileOperationNotification(operation, filePath, success);

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("success", true)));
        });

        app.post("/api/notifications/websocket", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String event = (String) request.get("event");
            String details = (String) request.get("details");

            if (event == null || details == null) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Event and details are required")));
                return;
            }

            notificationManager.showWebSocketNotification(event, details);

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("success", true)));
        });

        app.post("/api/notifications/system", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
            String status = (String) request.get("status");
            String details = (String) request.get("details");

            if (status == null || details == null) {
                ctx.status(400);
                ctx.contentType("application/json");
                ctx.result(gson.toJson(Map.of("error", "Status and details are required")));
                return;
            }

            notificationManager.showSystemNotification(status, details);

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("success", true)));
        });

        // Settings endpoints
        app.get("/api/settings/all", ctx -> {
            Properties settings = settingsManager.getAllSettings();
            Map<String, String> settingsMap = new HashMap<>();
            settings.forEach((key, value) -> settingsMap.put(key.toString(), value.toString()));
            ctx.contentType("application/json");
            ctx.result(gson.toJson(settingsMap));
        });

        app.post("/api/settings/save", ctx -> {
            Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);

            // Save each setting
            request.forEach((key, value) -> {
                if (value instanceof Boolean) {
                    settingsManager.setBoolean(key, (Boolean) value);
                } else if (value instanceof Number) {
                    settingsManager.setInt(key, ((Number) value).intValue());
                } else {
                    settingsManager.setString(key, value.toString());
                }
            });

            settingsManager.saveSettings();

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("success", true, "message", "Settings saved successfully")));
        });

        // Update manager endpoints
        app.get("/api/updates/check", ctx -> {
            Map<String, Object> updateInfo = updateManager.checkForUpdates();
            ctx.contentType("application/json");
            ctx.result(gson.toJson(updateInfo));
        });

        app.post("/api/updates/download", ctx -> {
            Map<String, Object> result = updateManager.downloadUpdate();
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        // Task manager endpoints
        app.get("/api/tasks", ctx -> {
            var tasks = taskManager.getActiveTasks();
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("tasks", tasks)));
        });

        app.get("/api/tasks/stats", ctx -> {
            var stats = taskManager.getTaskStatistics();
            ctx.contentType("application/json");
            ctx.result(gson.toJson(stats));
        });

        app.get("/api/tasks/{taskId}", ctx -> {
            String taskId = ctx.pathParam("taskId");
            var taskStatus = taskManager.getTaskStatus(taskId);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(taskStatus));
        });

        app.post("/api/tasks/submit", ctx -> {
            var request = gson.fromJson(ctx.body(), Map.class);
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            String type = (String) request.get("type");

            if (name == null || description == null) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("error", "Name and description are required")));
                return;
            }

            String taskId;
            switch (type != null ? type : "simple") {
                case "file-copy":
                    String source = (String) request.get("source");
                    String destination = (String) request.get("destination");
                    if (source == null || destination == null) {
                        ctx.status(400);
                        ctx.result(gson.toJson(Map.of("error", "Source and destination are required for file copy")));
                        return;
                    }
                    taskId = taskManager.submitProgressTask(name, description, progress -> {
                        var result = fileSystemManager.copy(source, destination);
                        if (!result.isSuccess()) {
                            throw new RuntimeException(result.getMessage());
                        }
                        return "File copied successfully";
                    });
                    break;

                case "file-move":
                    source = (String) request.get("source");
                    destination = (String) request.get("destination");
                    if (source == null || destination == null) {
                        ctx.status(400);
                        ctx.result(gson.toJson(Map.of("error", "Source and destination are required for file move")));
                        return;
                    }
                    taskId = taskManager.submitProgressTask(name, description, progress -> {
                        var result = fileSystemManager.move(source, destination);
                        if (!result.isSuccess()) {
                            throw new RuntimeException(result.getMessage());
                        }
                        return "File moved successfully";
                    });
                    break;

                case "update-download":
                    taskId = taskManager.submitProgressTask(name, description, progress -> {
                        var result = updateManager.downloadUpdate();
                        return result;
                    });
                    break;

                default:
                    // Simple task - just run a demo operation
                    taskId = taskManager.submitProgressTask(name, description, progress -> {
                        // Simulate some work
                        for (int i = 0; i <= 100; i += 10) {
                            progress.accept((double) i);
                            Thread.sleep(200);
                        }
                        return "Task completed successfully";
                    });
            }

            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("taskId", taskId, "status", "submitted")));
        });

        app.post("/api/tasks/{taskId}/cancel", ctx -> {
            String taskId = ctx.pathParam("taskId");
            boolean cancelled = taskManager.cancelTask(taskId);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("cancelled", cancelled)));
        });

        app.delete("/api/tasks/{taskId}", ctx -> {
            String taskId = ctx.pathParam("taskId");
            boolean removed = taskManager.removeTask(taskId);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("removed", removed)));
        });
    }

    public void start() {
        app.start(port);
        System.out.println("Backend server started on http://localhost:" + port);
    }

    public void stop() {
        app.stop();
    }

    public int getPort() {
        return port;
    }
}
