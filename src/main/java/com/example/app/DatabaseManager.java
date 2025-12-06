package com.example.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite database manager for local data storage
 * Handles application data persistence and user data
 */
public class DatabaseManager {
    private static final String DB_NAME = "java-webview.db";
    private static final String APP_DATA_DIR = ".java-webview-app";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Create app data directory
            Path appDataDir = Paths.get(System.getProperty("user.home"), APP_DATA_DIR);
            Files.createDirectories(appDataDir);

            // Database file path
            Path dbPath = appDataDir.resolve(DB_NAME);
            String dbUrl = "jdbc:sqlite:" + dbPath.toString();

            // Create connection
            connection = DriverManager.getConnection(dbUrl);
            System.out.println("Database initialized at: " + dbPath);

            // Create tables
            createTables();

            // Initialize default data
            initializeDefaultData();

        } catch (SQLException | IOException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Application logs table
        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS app_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                level TEXT NOT NULL,
                category TEXT,
                message TEXT NOT NULL,
                details TEXT
            );
            """;

        // User data table
        String createUserDataTable = """
            CREATE TABLE IF NOT EXISTS user_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT UNIQUE NOT NULL,
                value TEXT,
                data_type TEXT DEFAULT 'string',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        // File operations history
        String createFileHistoryTable = """
            CREATE TABLE IF NOT EXISTS file_operations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                operation_type TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_size INTEGER,
                success BOOLEAN DEFAULT 1,
                error_message TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        // WebSocket messages history
        String createWebSocketHistoryTable = """
            CREATE TABLE IF NOT EXISTS websocket_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                direction TEXT NOT NULL, -- 'sent' or 'received'
                message_type TEXT,
                content TEXT,
                session_id TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        // API calls history
        String createApiHistoryTable = """
            CREATE TABLE IF NOT EXISTS api_calls (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                method TEXT NOT NULL,
                endpoint TEXT NOT NULL,
                status_code INTEGER,
                response_time INTEGER, -- milliseconds
                success BOOLEAN DEFAULT 1,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        // Notifications history
        String createNotificationsTable = """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                message TEXT NOT NULL,
                success BOOLEAN DEFAULT 1,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        executeUpdate(createLogsTable);
        executeUpdate(createUserDataTable);
        executeUpdate(createFileHistoryTable);
        executeUpdate(createWebSocketHistoryTable);
        executeUpdate(createApiHistoryTable);
        executeUpdate(createNotificationsTable);

        System.out.println("Database tables created successfully");
    }

    private void initializeDefaultData() throws SQLException {
        // Insert some default user data if not exists
        String insertDefaultData = """
            INSERT OR IGNORE INTO user_data (key, value, data_type) VALUES
            ('app.firstRun', 'true', 'boolean'),
            ('app.version', '1.0.0', 'string'),
            ('ui.lastTheme', 'light', 'string'),
            ('stats.totalLaunches', '0', 'integer'),
            ('stats.totalWebSocketMessages', '0', 'integer');
            """;

        executeUpdate(insertDefaultData);

        // Log application startup
        logInfo("Application", "Database initialized successfully");
    }

    // Logging methods
    public void logInfo(String category, String message) {
        log("INFO", category, message, null);
    }

    public void logWarning(String category, String message) {
        log("WARNING", category, message, null);
    }

    public void logError(String category, String message, String details) {
        log("ERROR", category, message, details);
    }

    private void log(String level, String category, String message, String details) {
        String sql = "INSERT INTO app_logs (level, category, message, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, level);
            stmt.setString(2, category);
            stmt.setString(3, message);
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log message: " + e.getMessage());
        }
    }

    // User data methods
    public void setUserData(String key, String value, String dataType) {
        String sql = """
            INSERT INTO user_data (key, value, data_type, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(key) DO UPDATE SET
            value = excluded.value,
            data_type = excluded.data_type,
            updated_at = CURRENT_TIMESTAMP;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, dataType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database", "Failed to set user data: " + key, e.getMessage());
        }
    }

    public String getUserData(String key, String defaultValue) {
        String sql = "SELECT value FROM user_data WHERE key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get user data: " + key, e.getMessage());
        }
        return defaultValue;
    }

    public int getUserDataInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getUserData(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getUserDataBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getUserData(key, String.valueOf(defaultValue)));
    }

    // File operations history
    public void logFileOperation(String operationType, String filePath, long fileSize, boolean success, String errorMessage) {
        String sql = """
            INSERT INTO file_operations
            (operation_type, file_path, file_size, success, error_message)
            VALUES (?, ?, ?, ?, ?);
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, operationType);
            stmt.setString(2, filePath);
            stmt.setLong(3, fileSize);
            stmt.setBoolean(4, success);
            stmt.setString(5, errorMessage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database", "Failed to log file operation", e.getMessage());
        }
    }

    // Notification history
    public void logNotification(String type, String message, boolean success) {
        String sql = """
            INSERT INTO notifications
            (type, message, success)
            VALUES (?, ?, ?);
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, message);
            stmt.setBoolean(3, success);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database", "Failed to log notification", e.getMessage());
        }
    }

    // WebSocket messages history
    public void logWebSocketMessage(String direction, String messageType, String content, String sessionId) {
        String sql = """
            INSERT INTO websocket_messages
            (direction, message_type, content, session_id)
            VALUES (?, ?, ?, ?);
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, direction);
            stmt.setString(2, messageType);
            stmt.setString(3, content);
            stmt.setString(4, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database", "Failed to log WebSocket message", e.getMessage());
        }
    }

    // API calls history
    public void logApiCall(String method, String endpoint, int statusCode, long responseTime, boolean success) {
        String sql = """
            INSERT INTO api_calls
            (method, endpoint, status_code, response_time, success)
            VALUES (?, ?, ?, ?, ?);
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, method);
            stmt.setString(2, endpoint);
            stmt.setInt(3, statusCode);
            stmt.setLong(4, responseTime);
            stmt.setBoolean(5, success);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database", "Failed to log API call", e.getMessage());
        }
    }

    // Statistics methods
    public void incrementLaunchCount() {
        int current = getUserDataInt("stats.totalLaunches", 0);
        setUserData("stats.totalLaunches", String.valueOf(current + 1), "integer");
    }

    public void incrementWebSocketMessageCount() {
        int current = getUserDataInt("stats.totalWebSocketMessages", 0);
        setUserData("stats.totalWebSocketMessages", String.valueOf(current + 1), "integer");
    }

    public int getLaunchCount() {
        return getUserDataInt("stats.totalLaunches", 0);
    }

    public int getWebSocketMessageCount() {
        return getUserDataInt("stats.totalWebSocketMessages", 0);
    }

    // Query methods
    public List<Map<String, Object>> getRecentLogs(int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = """
            SELECT timestamp, level, category, message, details
            FROM app_logs
            ORDER BY timestamp DESC
            LIMIT ?;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("timestamp", rs.getString("timestamp"));
                log.put("level", rs.getString("level"));
                log.put("category", rs.getString("category"));
                log.put("message", rs.getString("message"));
                log.put("details", rs.getString("details"));
                logs.add(log);
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get recent logs", e.getMessage());
        }

        return logs;
    }

    public List<Map<String, Object>> getRecentFileOperations(int limit) {
        List<Map<String, Object>> operations = new ArrayList<>();
        String sql = """
            SELECT operation_type, file_path, file_size, success, error_message, timestamp
            FROM file_operations
            ORDER BY timestamp DESC
            LIMIT ?;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> op = new HashMap<>();
                op.put("operation_type", rs.getString("operation_type"));
                op.put("file_path", rs.getString("file_path"));
                op.put("file_size", rs.getLong("file_size"));
                op.put("success", rs.getBoolean("success"));
                op.put("error_message", rs.getString("error_message"));
                op.put("timestamp", rs.getString("timestamp"));
                operations.add(op);
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get recent file operations", e.getMessage());
        }

        return operations;
    }

    // API methods for REST endpoints
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // Count records in each table
            String[] tables = {"app_logs", "user_data", "file_operations", "websocket_messages", "api_calls", "notifications"};
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table;
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        stats.put(table + "_count", rs.getInt(1));
                    }
                }
            }

            // Database file size
            stats.put("database_path", getDatabasePath().toString());
            stats.put("database_size_kb", getDatabasePath().toFile().length() / 1024.0);
            stats.put("healthy", isHealthy());

        } catch (SQLException e) {
            logError("Database", "Failed to get database stats", e.getMessage());
        }
        return stats;
    }

    public Map<String, Object> getAllUserData() {
        Map<String, Object> userData = new HashMap<>();
        String sql = "SELECT key, value, data_type, created_at, updated_at FROM user_data ORDER BY key";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String key = rs.getString("key");
                String value = rs.getString("value");
                String dataType = rs.getString("data_type");

                // Convert value based on data type
                Object convertedValue = switch (dataType) {
                    case "integer" -> Integer.parseInt(value);
                    case "boolean" -> Boolean.parseBoolean(value);
                    case "float", "double" -> Double.parseDouble(value);
                    default -> value;
                };

                userData.put(key, Map.of(
                    "value", convertedValue,
                    "data_type", dataType,
                    "created_at", rs.getString("created_at"),
                    "updated_at", rs.getString("updated_at")
                ));
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get all user data", e.getMessage());
        }
        return userData;
    }

    public boolean deleteUserData(String key) {
        String sql = "DELETE FROM user_data WHERE key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logError("Database", "Failed to delete user data: " + key, e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getLogs(int limit) {
        return getRecentLogs(limit);
    }

    public List<Map<String, Object>> getNotifications(int limit) {
        List<Map<String, Object>> notifications = new ArrayList<>();
        String sql = """
            SELECT type, message, success, timestamp
            FROM notifications
            ORDER BY timestamp DESC
            LIMIT ?;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", rs.getString("type"));
                notification.put("message", rs.getString("message"));
                notification.put("success", rs.getBoolean("success"));
                notification.put("timestamp", rs.getString("timestamp"));
                notifications.add(notification);
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get notifications", e.getMessage());
        }

        return notifications;
    }

    public List<Map<String, Object>> getApiCalls(int limit) {
        List<Map<String, Object>> apiCalls = new ArrayList<>();
        String sql = """
            SELECT method, endpoint, status_code, response_time, success, timestamp
            FROM api_calls
            ORDER BY timestamp DESC
            LIMIT ?;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> apiCall = new HashMap<>();
                apiCall.put("method", rs.getString("method"));
                apiCall.put("endpoint", rs.getString("endpoint"));
                apiCall.put("status_code", rs.getInt("status_code"));
                apiCall.put("response_time", rs.getLong("response_time"));
                apiCall.put("success", rs.getBoolean("success"));
                apiCall.put("timestamp", rs.getString("timestamp"));
                apiCalls.add(apiCall);
            }
        } catch (SQLException e) {
            logError("Database", "Failed to get API calls", e.getMessage());
        }

        return apiCalls;
    }

    // Utility methods
    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Get database file path
    public Path getDatabasePath() {
        return Paths.get(System.getProperty("user.home"), APP_DATA_DIR, DB_NAME);
    }

    // Check if database is healthy
    public boolean isHealthy() {
        try {
            String sql = "SELECT COUNT(*) FROM app_logs";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
