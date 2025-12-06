package com.example.app;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream input = Launcher.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (Exception e) {
            logger.error("Failed to load config.properties", e);
        }

        String mode = config.getProperty("run.mode", "desktop"); // Default to desktop
        if ("web".equalsIgnoreCase(mode)) {
            Application.main(args); // Legacy browser mode
        } else {
            DesktopApplication.main(args); // Native desktop mode
        }
    }
}
