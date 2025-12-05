package com.example.app;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Update manager for handling application updates
 * Checks for new versions and downloads updates
 */
public class UpdateManager {
    private static UpdateManager instance;
    private static final String CURRENT_VERSION = "1.0.0";
    private static final String UPDATE_URL = "https://api.github.com/repos/phearun-coder/java-webview/releases/latest";
    private static final String DOWNLOAD_DIR = "updates";

    private UpdateManager() {
        // Create updates directory
        try {
            Path updateDir = Paths.get(System.getProperty("user.home"), ".java-webview-app", DOWNLOAD_DIR);
            Files.createDirectories(updateDir);
        } catch (IOException e) {
            System.err.println("Failed to create updates directory: " + e.getMessage());
        }
    }

    public static synchronized UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }

    /**
     * Check for updates
     */
    public Map<String, Object> checkForUpdates() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentVersion", CURRENT_VERSION);
        result.put("updateAvailable", false);

        try {
            // Simulate update check (in real implementation, this would check GitHub API)
            // For demo purposes, we'll randomly show an update as available
            boolean updateAvailable = Math.random() > 0.7; // 30% chance of update

            if (updateAvailable) {
                result.put("updateAvailable", true);
                result.put("latestVersion", "1.1.0");
                result.put("releaseNotes", "Bug fixes and performance improvements");
                result.put("downloadUrl", "https://example.com/download/java-webview-1.1.0.jar");
            }

            DatabaseManager.getInstance().logApiCall("GET", "/api/updates/check", 200, 0L, true);

        } catch (Exception e) {
            result.put("error", "Failed to check for updates: " + e.getMessage());
            DatabaseManager.getInstance().logApiCall("GET", "/api/updates/check", 500, 0L, false);
        }

        return result;
    }

    /**
     * Download update
     */
    public Map<String, Object> downloadUpdate() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            // Check if update is available first
            Map<String, Object> updateInfo = checkForUpdates();

            if (!(Boolean) updateInfo.get("updateAvailable")) {
                result.put("message", "No update available");
                return result;
            }

            String downloadUrl = (String) updateInfo.get("downloadUrl");
            String version = (String) updateInfo.get("latestVersion");

            if (downloadUrl == null || version == null) {
                result.put("message", "Invalid update information");
                return result;
            }

            // Simulate download (in real implementation, this would download from URL)
            Path updateDir = Paths.get(System.getProperty("user.home"), ".java-webview-app", DOWNLOAD_DIR);
            Path updateFile = updateDir.resolve("java-webview-" + version + ".jar");

            // Create a dummy file to simulate download
            Files.writeString(updateFile, "This is a simulated update file for version " + version);

            result.put("success", true);
            result.put("message", "Update downloaded successfully");
            result.put("filePath", updateFile.toString());
            result.put("version", version);

            // Log the successful download
            DatabaseManager.getInstance().logApiCall("POST", "/api/updates/download", 200, 0L, true);

            // Show notification
            NotificationManager.getInstance().showUpdateNotification(version, "Update downloaded successfully");

        } catch (Exception e) {
            result.put("message", "Failed to download update: " + e.getMessage());
            DatabaseManager.getInstance().logApiCall("POST", "/api/updates/download", 500, 0L, false);
        }

        return result;
    }

    /**
     * Get update download directory
     */
    public Path getUpdateDirectory() {
        return Paths.get(System.getProperty("user.home"), ".java-webview-app", DOWNLOAD_DIR);
    }

    /**
     * Check if update file exists
     */
    public boolean updateFileExists(String version) {
        Path updateFile = getUpdateDirectory().resolve("java-webview-" + version + ".jar");
        return Files.exists(updateFile);
    }

    /**
     * Get available update versions
     */
    public List<String> getAvailableUpdates() {
        List<String> updates = new ArrayList<>();

        try {
            Path updateDir = getUpdateDirectory();
            if (Files.exists(updateDir)) {
                try (var stream = Files.list(updateDir)) {
                    stream.filter(Files::isRegularFile)
                          .filter(path -> path.toString().endsWith(".jar"))
                          .forEach(path -> {
                              String fileName = path.getFileName().toString();
                              // Extract version from filename (e.g., "java-webview-1.1.0.jar" -> "1.1.0")
                              String version = fileName.replace("java-webview-", "").replace(".jar", "");
                              updates.add(version);
                          });
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to list available updates: " + e.getMessage());
        }

        return updates;
    }

    /**
     * Clean old update files
     */
    public void cleanOldUpdates() {
        try {
            Path updateDir = getUpdateDirectory();
            if (Files.exists(updateDir)) {
                try (var stream = Files.list(updateDir)) {
                    stream.filter(Files::isRegularFile)
                          .filter(path -> path.toString().endsWith(".jar"))
                          .filter(path -> !path.getFileName().toString().contains(CURRENT_VERSION))
                          .forEach(path -> {
                              try {
                                  Files.delete(path);
                                  System.out.println("Cleaned old update file: " + path.getFileName());
                              } catch (IOException e) {
                                  System.err.println("Failed to delete old update file: " + path.getFileName());
                              }
                          });
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to clean old updates: " + e.getMessage());
        }
    }

    /**
     * Get current version
     */
    public String getCurrentVersion() {
        return CURRENT_VERSION;
    }

    /**
     * Compare versions (simple implementation)
     */
    public int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        for (int i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
            int v1 = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2 = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1 != v2) {
                return Integer.compare(v1, v2);
            }
        }

        return 0;
    }

    /**
     * Check for updates asynchronously
     */
    public CompletableFuture<Map<String, Object>> checkForUpdatesAsync() {
        return CompletableFuture.supplyAsync(this::checkForUpdates);
    }

    /**
     * Download update asynchronously
     */
    public CompletableFuture<Map<String, Object>> downloadUpdateAsync() {
        return CompletableFuture.supplyAsync(this::downloadUpdate);
    }
}
