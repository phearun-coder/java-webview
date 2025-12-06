package com.example.app;

import java.awt.Desktop;
import java.net.URI;
import java.net.ServerSocket;
import java.io.IOException;

/**
 * Main Application class
 * Starts the Java backend server and opens the UI in the system browser
 * This approach works cross-platform without external dependencies
 */
public class Application {
    private static final int DEFAULT_BACKEND_PORT = 8080;
    private static int BACKEND_PORT;
    private static BackendServer server;

    public static void main(String[] args) {
        System.out.println("Starting Java WebView Application...");

        // Auto-detect available port
        BACKEND_PORT = findAvailablePort(DEFAULT_BACKEND_PORT);
        System.out.println("Using port: " + BACKEND_PORT);

        // Start backend server
        startBackendServer();

        // Wait for server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Open browser with better environment detection
        openBrowser("http://localhost:" + BACKEND_PORT + "/index.html");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 Application is running!");
        System.out.println("📱 UI: http://localhost:" + BACKEND_PORT);
        System.out.println("🔌 API: http://localhost:" + BACKEND_PORT + "/api");
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println("=".repeat(60) + "\n");

        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void startBackendServer() {
        Thread serverThread = new Thread(() -> {
            server = new BackendServer(BACKEND_PORT);
            server.start();
        });
        serverThread.setDaemon(false); // Changed to false to keep app running
        serverThread.start();

        // Add shutdown hook to stop server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
                System.out.println("\nStopping backend server...");
                server.stop();
            }
        }));
    }

    private static int findAvailablePort(int startPort) {
        for (int port = startPort; port < startPort + 100; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port; // Port is available
            } catch (IOException e) {
                // Port is in use, try next
                System.out.println("Port " + port + " is in use, trying next...");
            }
        }
        throw new RuntimeException("No available ports found in range " + startPort + " - " + (startPort + 99));
    }

    private static void openBrowser(String url) {
        // Check if we're in a headless/container environment
        if (isHeadlessEnvironment()) {
            System.out.println("⚠️  Running in headless/container environment.");
            System.out.println("💡 Browser auto-launch disabled. Please open manually:");
            System.out.println("   " + url);
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("✓ Opened browser at: " + url);
            } else {
                // Fallback for systems without Desktop support using ProcessBuilder
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder processBuilder;
                
                if (os.contains("win")) {
                    processBuilder = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
                } else if (os.contains("mac")) {
                    processBuilder = new ProcessBuilder("open", url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    processBuilder = new ProcessBuilder("xdg-open", url);
                } else {
                    throw new UnsupportedOperationException("Unsupported operating system");
                }
                
                processBuilder.start();
                System.out.println("✓ Opened browser at: " + url);
            }
        } catch (Exception e) {
            System.err.println("Could not automatically open browser.");
            System.err.println("Please open manually: " + url);
        }
    }

    private static boolean isHeadlessEnvironment() {
        // Check for common headless/container indicators
        String display = System.getenv("DISPLAY");
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String xdgSessionType = System.getenv("XDG_SESSION_TYPE");
        String term = System.getenv("TERM");
        
        // Check if running in Docker/container
        boolean hasContainerEnv = System.getenv("DOCKER_CONTAINER") != null ||
                                  System.getenv("CONTAINER") != null ||
                                  new java.io.File("/.dockerenv").exists();
        
        // Check for headless Java property
        boolean isJavaHeadless = Boolean.getBoolean("java.awt.headless");
        
        // Check for display environment
        boolean hasDisplay = (display != null && !display.isEmpty()) ||
                            (waylandDisplay != null && !waylandDisplay.isEmpty()) ||
                            "wayland".equals(xdgSessionType) ||
                            "x11".equals(xdgSessionType);
        
        // If no display and in container, or explicitly headless
        return (hasContainerEnv && !hasDisplay) || isJavaHeadless || "dumb".equals(term);
    }
}
