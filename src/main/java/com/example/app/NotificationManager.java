package com.example.app;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.CompletableFuture;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Native notifications manager
 * Provides system tray notifications for the desktop application
 */
public class NotificationManager {
    private static NotificationManager instance;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private boolean initialized = false;

    private NotificationManager() {
        initializeTray();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Initialize system tray
     */
    private void initializeTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this platform");
            return;
        }

        try {
            systemTray = SystemTray.getSystemTray();

            // Create a default icon (simple colored square)
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(new Color(0, 123, 255)); // Bootstrap primary blue
            g2d.fillRect(0, 0, 16, 16);
            g2d.setColor(Color.WHITE);
            g2d.drawString("J", 4, 12);
            g2d.dispose();

            trayIcon = new TrayIcon(image, "Java WebView App");
            trayIcon.setImageAutoSize(true);

            // Add tooltip
            trayIcon.setToolTip("Java WebView Application");

            systemTray.add(trayIcon);
            initialized = true;

            // Log initialization
            DatabaseManager.getInstance().logNotification("system", "Tray initialized", true);

        } catch (AWTException e) {
            System.err.println("Failed to initialize system tray: " + e.getMessage());
            DatabaseManager.getInstance().logNotification("system", "Tray initialization failed: " + e.getMessage(), false);
        }
    }

    /**
     * Show a notification
     */
    public boolean showNotification(String title, String message) {
        return showNotification(title, message, MessageType.INFO);
    }

    /**
     * Show a notification with specific type
     */
    public boolean showNotification(String title, String message, MessageType type) {
        if (!initialized || trayIcon == null) {
            System.err.println("Tray not initialized, cannot show notification");
            DatabaseManager.getInstance().logNotification("notification", title + ": " + message, false);
            return false;
        }

        try {
            trayIcon.displayMessage(title, message, type);
            DatabaseManager.getInstance().logNotification("notification", title + ": " + message, true);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to show notification: " + e.getMessage());
            DatabaseManager.getInstance().logNotification("notification", title + ": " + message + " (error: " + e.getMessage() + ")", false);
            return false;
        }
    }

    /**
     * Show info notification
     */
    public boolean showInfo(String title, String message) {
        return showNotification(title, message, MessageType.INFO);
    }

    /**
     * Show warning notification
     */
    public boolean showWarning(String title, String message) {
        return showNotification(title, message, MessageType.WARNING);
    }

    /**
     * Show error notification
     */
    public boolean showError(String title, String message) {
        return showNotification(title, message, MessageType.ERROR);
    }

    /**
     * Show success notification
     */
    public boolean showSuccess(String title, String message) {
        return showNotification(title, message, MessageType.INFO);
    }

    /**
     * Show file operation notification
     */
    public void showFileOperationNotification(String operation, String filePath, boolean success) {
        String title = success ? "File Operation Successful" : "File Operation Failed";
        String message = operation.substring(0, 1).toUpperCase() + operation.substring(1) + ": " +
                        new java.io.File(filePath).getName();

        if (success) {
            showSuccess(title, message);
        } else {
            showError(title, message);
        }
    }

    /**
     * Show WebSocket notification
     */
    public void showWebSocketNotification(String event, String details) {
        String title = "WebSocket " + event;
        showInfo(title, details);
    }

    /**
     * Show application update notification
     */
    public void showUpdateNotification(String version, String changelog) {
        String title = "Update Available";
        String message = "Version " + version + " is available. " + changelog;
        showInfo(title, message);
    }

    /**
     * Show system status notification
     */
    public void showSystemNotification(String status, String details) {
        String title = "System " + status;
        showInfo(title, details);
    }

    /**
     * Check if notifications are supported
     */
    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    /**
     * Check if tray is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Remove tray icon and cleanup
     */
    public void cleanup() {
        if (initialized && systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            initialized = false;
            DatabaseManager.getInstance().logNotification("system", "Tray cleanup completed", true);
        }
    }

    /**
     * Get notification types available
     */
    public MessageType[] getAvailableTypes() {
        return new MessageType[]{MessageType.INFO, MessageType.WARNING, MessageType.ERROR, MessageType.NONE};
    }

    /**
     * Show notification asynchronously
     */
    public CompletableFuture<Boolean> showNotificationAsync(String title, String message) {
        return CompletableFuture.supplyAsync(() -> showNotification(title, message));
    }

    /**
     * Show notification with custom icon (if supported)
     */
    public boolean showNotificationWithIcon(String title, String message, Image icon) {
        if (!initialized || trayIcon == null) {
            return false;
        }

        try {
            TrayIcon tempIcon = new TrayIcon(icon != null ? icon : trayIcon.getImage(), "Java WebView App");
            tempIcon.setImageAutoSize(true);
            systemTray.add(tempIcon);

            tempIcon.displayMessage(title, message, MessageType.INFO);

            // Remove after a delay
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    systemTray.remove(tempIcon);
                }
            }, 5000); // Remove after 5 seconds

            DatabaseManager.getInstance().logNotification("notification", title + ": " + message + " (custom icon)", true);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to show notification with custom icon: " + e.getMessage());
            DatabaseManager.getInstance().logNotification("notification", title + ": " + message + " (custom icon error: " + e.getMessage() + ")", false);
            return false;
        }
    }
}
