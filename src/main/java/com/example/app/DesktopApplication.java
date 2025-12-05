package com.example.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.PopupMenu;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Native Desktop Application with JavaFX WebView
 * Provides a true desktop application experience with embedded browser
 */
public class DesktopApplication extends Application {
    private static final int BACKEND_PORT = 8080;
    private static BackendServer server;
    private TrayIcon trayIcon;
    private List<Stage> windows = new ArrayList<>();
    private int windowCounter = 1;

    @Override
    public void start(Stage primaryStage) {
        this.windows.add(primaryStage);

        System.out.println("Starting Java WebView Desktop Application...");

        // Start backend server in background
        startBackendServer();

        // Setup system tray
        setupSystemTray();

        // Create JavaFX UI for primary window
        createUI(primaryStage, "Main Window");

        // Handle application close
        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Prevent default close
            handleWindowClose(primaryStage);
        });
    }

    private void createUI(Stage stage, String title) {
        // Create WebView
        WebView webView = new WebView();
        webView.getEngine().load("http://localhost:" + BACKEND_PORT + "/index.html");

        // Enable JavaScript and modern features
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().setUserAgent("JavaWebView/1.0");

        // Create menu bar
        MenuBar menuBar = createMenuBar(stage);

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(webView);

        // Scene
        Scene scene = new Scene(root, 1200, 800);

        // Stage setup
        stage.setTitle("Java WebView - " + title);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        System.out.println("Created window: " + title);
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newWindowItem = new MenuItem("New Window");
        newWindowItem.setOnAction(e -> createNewWindow());
        MenuItem closeWindowItem = new MenuItem("Close Window");
        closeWindowItem.setOnAction(e -> handleWindowClose(stage));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> exitApplication());
        fileMenu.getItems().addAll(newWindowItem, new SeparatorMenuItem(), closeWindowItem, new SeparatorMenuItem(), exitItem);

        // Window menu
        Menu windowMenu = new Menu("Window");
        MenuItem minimizeItem = new MenuItem("Minimize");
        minimizeItem.setOnAction(e -> stage.setIconified(true));
        MenuItem maximizeItem = new MenuItem("Maximize");
        maximizeItem.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        MenuItem showTrayItem = new MenuItem("Minimize to Tray");
        showTrayItem.setOnAction(e -> minimizeToTray());
        windowMenu.getItems().addAll(minimizeItem, maximizeItem, new SeparatorMenuItem(), showTrayItem);

        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem reloadItem = new MenuItem("Reload");
        reloadItem.setOnAction(e -> {
            // Reload WebView
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            WebView webView = (WebView) root.getCenter();
            webView.getEngine().reload();
        });
        MenuItem devToolsItem = new MenuItem("Developer Tools");
        devToolsItem.setOnAction(e -> {
            // Open developer tools (if supported)
            System.out.println("Developer tools not available in JavaFX WebView");
        });
        viewMenu.getItems().addAll(reloadItem, devToolsItem);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAbout());
        MenuItem windowsItem = new MenuItem("Show All Windows");
        windowsItem.setOnAction(e -> showAllWindows());
        helpMenu.getItems().addAll(aboutItem, new SeparatorMenuItem(), windowsItem);

        menuBar.getMenus().addAll(fileMenu, windowMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported on this platform");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Create tray icon (using a default icon)
            Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            trayIcon = new TrayIcon(image, "Java WebView App");
            trayIcon.setImageAutoSize(true);

            // Create popup menu
            PopupMenu popup = new PopupMenu();
            java.awt.MenuItem showItem = new java.awt.MenuItem("Show");
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");

            showItem.addActionListener(e -> Platform.runLater(this::showFromTray));
            exitItem.addActionListener(e -> exitApplication());

            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);
            trayIcon.addActionListener(e -> Platform.runLater(this::showFromTray));

            tray.add(trayIcon);
            System.out.println("System tray icon added");

        } catch (Exception e) {
            System.err.println("Failed to setup system tray: " + e.getMessage());
        }
    }

    private void createNewWindow() {
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            windows.add(newStage);
            String title = "Window " + windowCounter++;
            createUI(newStage, title);

            newStage.setOnCloseRequest(e -> {
                e.consume();
                handleWindowClose(newStage);
            });
        });
    }

    private void handleWindowClose(Stage stage) {
        if (windows.size() > 1) {
            // Just close this window
            windows.remove(stage);
            stage.close();
            System.out.println("Closed window. Remaining windows: " + windows.size());
        } else {
            // Last window, minimize to tray instead
            minimizeToTray();
        }
    }

    private void showAllWindows() {
        Platform.runLater(() -> {
            for (Stage window : windows) {
                if (!window.isShowing()) {
                    window.show();
                }
                window.toFront();
            }
        });
    }

    private void minimizeToTray() {
        Platform.runLater(() -> {
            for (Stage window : windows) {
                window.hide();
            }
            if (trayIcon != null) {
                trayIcon.displayMessage("Java WebView App",
                    "All windows minimized to system tray",
                    TrayIcon.MessageType.INFO);
            }
        });
    }

    private void showFromTray() {
        Platform.runLater(() -> {
            for (Stage window : windows) {
                window.show();
                window.toFront();
            }
        });
    }

    private void showAbout() {
        // Simple about dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Java WebView Desktop Application");
        alert.setContentText("Version 1.0.0\nBuilt with JavaFX WebView\nReal-time WebSocket communication");
        alert.showAndWait();
    }

    private void exitApplication() {
        System.out.println("Shutting down application...");

        // Stop server
        if (server != null) {
            server.stop();
        }

        // Remove tray icon
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }

        Platform.exit();
        System.exit(0);
    }

    private void startBackendServer() {
        Thread serverThread = new Thread(() -> {
            try {
                server = new BackendServer(BACKEND_PORT);
                server.start();
            } catch (Exception e) {
                System.err.println("Failed to start backend server: " + e);
                Platform.exit();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}