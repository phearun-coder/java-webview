package com.example.app;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Application settings and preferences manager
 * Provides persistent storage and runtime configuration
 */
public class SettingsManager {
    private static final String SETTINGS_FILE = "java-webview-settings.properties";
    private static final String APP_DATA_DIR = ".java-webview-app";

    private static SettingsManager instance;
    private final ConcurrentMap<String, Object> runtimeSettings = new ConcurrentHashMap<>();
    private final Properties persistentSettings = new Properties();
    private Path settingsPath;

    private SettingsManager() {
        initializeSettings();
        loadSettings();
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private void initializeSettings() {
        try {
            // Create app data directory in user home
            Path appDataDir = Paths.get(System.getProperty("user.home"), APP_DATA_DIR);
            Files.createDirectories(appDataDir);

            // Settings file path
            settingsPath = appDataDir.resolve(SETTINGS_FILE);

            System.out.println("Settings directory: " + appDataDir);
            System.out.println("Settings file: " + settingsPath);

        } catch (IOException e) {
            System.err.println("Failed to initialize settings directory: " + e.getMessage());
            // Fallback to current directory
            settingsPath = Paths.get(SETTINGS_FILE);
        }
    }

    private void loadSettings() {
        if (Files.exists(settingsPath)) {
            try (FileInputStream fis = new FileInputStream(settingsPath.toFile())) {
                persistentSettings.load(fis);
                System.out.println("Loaded " + persistentSettings.size() + " settings from " + settingsPath);
            } catch (IOException e) {
                System.err.println("Failed to load settings: " + e.getMessage());
            }
        } else {
            // Set default values
            setDefaultSettings();
            saveSettings();
        }
    }

    private void setDefaultSettings() {
        // Window settings
        persistentSettings.setProperty("window.width", "1200");
        persistentSettings.setProperty("window.height", "800");
        persistentSettings.setProperty("window.x", "100");
        persistentSettings.setProperty("window.y", "100");
        persistentSettings.setProperty("window.maximized", "false");

        // Application settings
        persistentSettings.setProperty("theme", "light");
        persistentSettings.setProperty("autoStart", "false");
        persistentSettings.setProperty("minimizeToTray", "true");
        persistentSettings.setProperty("checkForUpdates", "true");

        // Server settings
        persistentSettings.setProperty("server.port", "8080");
        persistentSettings.setProperty("server.host", "localhost");

        // WebSocket settings
        persistentSettings.setProperty("websocket.autoReconnect", "true");
        persistentSettings.setProperty("websocket.reconnectDelay", "3000");

        // UI settings
        persistentSettings.setProperty("ui.showNotifications", "true");
        persistentSettings.setProperty("ui.animationEnabled", "true");
        persistentSettings.setProperty("ui.fontSize", "14");

        System.out.println("Set default settings");
    }

    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(settingsPath.toFile())) {
            persistentSettings.store(fos, "Java WebView Application Settings");
            System.out.println("Saved " + persistentSettings.size() + " settings to " + settingsPath);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    // Generic getter/setter methods
    public String getString(String key, String defaultValue) {
        return persistentSettings.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
    }

    public void setString(String key, String value) {
        persistentSettings.setProperty(key, value);
    }

    public void setInt(String key, int value) {
        setString(key, String.valueOf(value));
    }

    public void setDouble(String key, double value) {
        setString(key, String.valueOf(value));
    }

    public void setBoolean(String key, boolean value) {
        setString(key, String.valueOf(value));
    }

    // Runtime settings (not persisted)
    public Object getRuntime(String key) {
        return runtimeSettings.get(key);
    }

    public void setRuntime(String key, Object value) {
        runtimeSettings.put(key, value);
    }

    public void removeRuntime(String key) {
        runtimeSettings.remove(key);
    }

    // Convenience methods for common settings
    public int getWindowWidth() {
        return getInt("window.width", 1200);
    }

    public void setWindowWidth(int width) {
        setInt("window.width", width);
    }

    public int getWindowHeight() {
        return getInt("window.height", 800);
    }

    public void setWindowHeight(int height) {
        setInt("window.height", height);
    }

    public int getWindowX() {
        return getInt("window.x", 100);
    }

    public void setWindowX(int x) {
        setInt("window.x", x);
    }

    public int getWindowY() {
        return getInt("window.y", 100);
    }

    public void setWindowY(int y) {
        setInt("window.y", y);
    }

    public boolean isWindowMaximized() {
        return getBoolean("window.maximized", false);
    }

    public void setWindowMaximized(boolean maximized) {
        setBoolean("window.maximized", maximized);
    }

    public String getTheme() {
        return getString("theme", "light");
    }

    public void setTheme(String theme) {
        setString("theme", theme);
    }

    public boolean isMinimizeToTray() {
        return getBoolean("minimizeToTray", true);
    }

    public void setMinimizeToTray(boolean minimizeToTray) {
        setBoolean("minimizeToTray", minimizeToTray);
    }

    public boolean isAutoStart() {
        return getBoolean("autoStart", false);
    }

    public void setAutoStart(boolean autoStart) {
        setBoolean("autoStart", autoStart);
    }

    public boolean isCheckForUpdates() {
        return getBoolean("checkForUpdates", true);
    }

    public void setCheckForUpdates(boolean checkForUpdates) {
        setBoolean("checkForUpdates", checkForUpdates);
    }

    public int getServerPort() {
        return getInt("server.port", 8080);
    }

    public void setServerPort(int port) {
        setInt("server.port", port);
    }

    public String getServerHost() {
        return getString("server.host", "localhost");
    }

    public void setServerHost(String host) {
        setString("server.host", host);
    }

    public boolean isWebSocketAutoReconnect() {
        return getBoolean("websocket.autoReconnect", true);
    }

    public void setWebSocketAutoReconnect(boolean autoReconnect) {
        setBoolean("websocket.autoReconnect", autoReconnect);
    }

    public int getWebSocketReconnectDelay() {
        return getInt("websocket.reconnectDelay", 3000);
    }

    public void setWebSocketReconnectDelay(int delay) {
        setInt("websocket.reconnectDelay", delay);
    }

    public boolean isShowNotifications() {
        return getBoolean("ui.showNotifications", true);
    }

    public void setShowNotifications(boolean showNotifications) {
        setBoolean("ui.showNotifications", showNotifications);
    }

    public boolean isAnimationEnabled() {
        return getBoolean("ui.animationEnabled", true);
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        setBoolean("ui.animationEnabled", animationEnabled);
    }

    public int getFontSize() {
        return getInt("ui.fontSize", 14);
    }

    public void setFontSize(int fontSize) {
        setInt("ui.fontSize", fontSize);
    }

    // Get settings file path for external access
    public Path getSettingsPath() {
        return settingsPath;
    }

    // Reset to defaults
    public void resetToDefaults() {
        persistentSettings.clear();
        setDefaultSettings();
        saveSettings();
    }

    // Get all settings as Properties (for debugging)
    public Properties getAllSettings() {
        Properties copy = new Properties();
        copy.putAll(persistentSettings);
        return copy;
    }
}
