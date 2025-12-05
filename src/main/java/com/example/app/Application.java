package com.example.app;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main Application class
 * Starts the Java backend server and opens the UI in the system browser
 * This approach works cross-platform without external dependencies
 */
public class Application {
    private static final int BACKEND_PORT = 8080;
    private static BackendServer server;

    public static void main(String[] args) {
        System.out.println("Starting Java WebView Application...");

        // Start backend server
        startBackendServer();

        // Wait for server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Open browser
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

    private static void openBrowser(String url) {
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
}
