package com.example.app;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

/**
 * File system operations manager
 * Provides safe file operations with logging and user-friendly error handling
 */
public class FileSystemManager {
    private static FileSystemManager instance;
    private final DatabaseManager dbManager;
    private final NotificationManager notificationManager;

    private FileSystemManager() {
        dbManager = DatabaseManager.getInstance();
        notificationManager = NotificationManager.getInstance();
    }

    public static synchronized FileSystemManager getInstance() {
        if (instance == null) {
            instance = new FileSystemManager();
        }
        return instance;
    }

    /**
     * File operation result
     */
    public static class FileOperationResult {
        private final boolean success;
        private final String message;
        private final long fileSize;
        private final Exception exception;

        public FileOperationResult(boolean success, String message, long fileSize, Exception exception) {
            this.success = success;
            this.message = message;
            this.fileSize = fileSize;
            this.exception = exception;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getFileSize() { return fileSize; }
        public Exception getException() { return exception; }
    }

    /**
     * Directory listing result
     */
    public static class DirectoryListing {
        private final boolean success;
        private final List<FileInfo> files;
        private final String errorMessage;

        public DirectoryListing(boolean success, List<FileInfo> files, String errorMessage) {
            this.success = success;
            this.files = files != null ? files : new ArrayList<>();
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public List<FileInfo> getFiles() { return files; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * File information
     */
    public static class FileInfo {
        private final String name;
        private final String path;
        private final boolean isDirectory;
        private final long size;
        private final String lastModified;
        private final boolean readable;
        private final boolean writable;

        public FileInfo(String name, String path, boolean isDirectory, long size,
                       String lastModified, boolean readable, boolean writable) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
            this.size = size;
            this.lastModified = lastModified;
            this.readable = readable;
            this.writable = writable;
        }

        public String getName() { return name; }
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public String getLastModified() { return lastModified; }
        public boolean isReadable() { return readable; }
        public boolean isWritable() { return writable; }

        public String getSizeFormatted() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Read text file
     */
    public FileOperationResult readTextFile(String filePath) {
        Path path = Paths.get(filePath);
        long fileSize = 0;

        try {
            if (!Files.exists(path)) {
                return new FileOperationResult(false, "File does not exist: " + filePath, 0, null);
            }

            if (!Files.isReadable(path)) {
                return new FileOperationResult(false, "File is not readable: " + filePath, 0, null);
            }

            fileSize = Files.size(path);
            String content = Files.readString(path);

            dbManager.logFileOperation("read", filePath, fileSize, true, null);
            return new FileOperationResult(true, content, fileSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("read", filePath, fileSize, false, e.getMessage());
            notificationManager.showFileOperationNotification("read", filePath, false);
            return new FileOperationResult(false, "Failed to read file: " + e.getMessage(), fileSize, e);
        }
    }

    /**
     * Write text file
     */
    public FileOperationResult writeTextFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        long fileSize = content != null ? content.length() : 0;

        try {
            // Create parent directories if they don't exist
            Files.createDirectories(path.getParent());

            Files.writeString(path, content != null ? content : "");
            fileSize = Files.size(path);

            dbManager.logFileOperation("write", filePath, fileSize, true, null);
            notificationManager.showFileOperationNotification("write", filePath, true);
            return new FileOperationResult(true, "File written successfully", fileSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("write", filePath, fileSize, false, e.getMessage());
            notificationManager.showFileOperationNotification("write", filePath, false);
            return new FileOperationResult(false, "Failed to write file: " + e.getMessage(), fileSize, e);
        }
    }

    /**
     * Append to text file
     */
    public FileOperationResult appendTextFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        long originalSize = 0;

        try {
            if (Files.exists(path)) {
                originalSize = Files.size(path);
            }

            Files.writeString(path, content != null ? content : "",
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);

            long newSize = Files.size(path);
            dbManager.logFileOperation("append", filePath, newSize - originalSize, true, null);
            return new FileOperationResult(true, "Content appended successfully", newSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("append", filePath, originalSize, false, e.getMessage());
            return new FileOperationResult(false, "Failed to append to file: " + e.getMessage(), originalSize, e);
        }
    }

    /**
     * Delete file or directory
     */
    public FileOperationResult delete(String path) {
        Path filePath = Paths.get(path);
        long fileSize = 0;

        try {
            if (!Files.exists(filePath)) {
                return new FileOperationResult(false, "Path does not exist: " + path, 0, null);
            }

            if (Files.isDirectory(filePath)) {
                // Delete directory recursively
                long[] sizeHolder = {0};
                Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        sizeHolder[0] += attrs.size();
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                fileSize = sizeHolder[0];
            } else {
                fileSize = Files.size(filePath);
                Files.delete(filePath);
            }

            dbManager.logFileOperation("delete", path, fileSize, true, null);
            notificationManager.showFileOperationNotification("delete", path, true);
            return new FileOperationResult(true, "Deleted successfully", fileSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("delete", path, fileSize, false, e.getMessage());
            notificationManager.showFileOperationNotification("delete", path, false);
            return new FileOperationResult(false, "Failed to delete: " + e.getMessage(), fileSize, e);
        }
    }

    /**
     * Create directory
     */
    public FileOperationResult createDirectory(String dirPath) {
        Path path = Paths.get(dirPath);

        try {
            if (Files.exists(path) && Files.isDirectory(path)) {
                return new FileOperationResult(true, "Directory already exists", 0, null);
            }

            Files.createDirectories(path);
            dbManager.logFileOperation("create_dir", dirPath, 0, true, null);
            return new FileOperationResult(true, "Directory created successfully", 0, null);

        } catch (IOException e) {
            dbManager.logFileOperation("create_dir", dirPath, 0, false, e.getMessage());
            return new FileOperationResult(false, "Failed to create directory: " + e.getMessage(), 0, e);
        }
    }

    /**
     * List directory contents
     */
    public DirectoryListing listDirectory(String dirPath) {
        Path path = Paths.get(dirPath);
        List<FileInfo> files = new ArrayList<>();

        try {
            if (!Files.exists(path)) {
                return new DirectoryListing(false, null, "Directory does not exist: " + dirPath);
            }

            if (!Files.isDirectory(path)) {
                return new DirectoryListing(false, null, "Path is not a directory: " + dirPath);
            }

            if (!Files.isReadable(path)) {
                return new DirectoryListing(false, null, "Directory is not readable: " + dirPath);
            }

            try (Stream<Path> stream = Files.list(path)) {
                stream.sorted((p1, p2) -> {
                    // Directories first, then files, both alphabetically
                    boolean p1IsDir = Files.isDirectory(p1);
                    boolean p2IsDir = Files.isDirectory(p2);
                    if (p1IsDir && !p2IsDir) return -1;
                    if (!p1IsDir && p2IsDir) return 1;
                    return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
                }).forEach(filePath -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                        String lastModified = attrs.lastModifiedTime().toString().substring(0, 19);

                        FileInfo info = new FileInfo(
                            filePath.getFileName().toString(),
                            filePath.toString(),
                            Files.isDirectory(filePath),
                            attrs.size(),
                            lastModified,
                            Files.isReadable(filePath),
                            Files.isWritable(filePath)
                        );
                        files.add(info);
                    } catch (IOException e) {
                        System.err.println("Error reading file attributes: " + filePath + " - " + e.getMessage());
                    }
                });
            }

            return new DirectoryListing(true, files, null);

        } catch (IOException e) {
            return new DirectoryListing(false, null, "Failed to list directory: " + e.getMessage());
        }
    }

    /**
     * Copy file or directory
     */
    public FileOperationResult copy(String sourcePath, String destPath) {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);
        long totalSize = 0;

        try {
            if (!Files.exists(source)) {
                return new FileOperationResult(false, "Source does not exist: " + sourcePath, 0, null);
            }

            // Calculate total size
            if (Files.isDirectory(source)) {
                totalSize = Files.walk(source)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            } else {
                totalSize = Files.size(source);
            }

            // Perform copy
            if (Files.isDirectory(source)) {
                copyDirectory(source, dest);
            } else {
                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            dbManager.logFileOperation("copy", sourcePath + " -> " + destPath, totalSize, true, null);
            return new FileOperationResult(true, "Copied successfully", totalSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("copy", sourcePath + " -> " + destPath, totalSize, false, e.getMessage());
            return new FileOperationResult(false, "Failed to copy: " + e.getMessage(), totalSize, e);
        }
    }

    private void copyDirectory(Path source, Path dest) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path target = dest.resolve(source.relativize(dir));
                Files.createDirectories(target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dest.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Move/rename file or directory
     */
    public FileOperationResult move(String sourcePath, String destPath) {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);
        long fileSize = 0;

        try {
            if (!Files.exists(source)) {
                return new FileOperationResult(false, "Source does not exist: " + sourcePath, 0, null);
            }

            if (Files.isDirectory(source)) {
                fileSize = Files.walk(source)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            } else {
                fileSize = Files.size(source);
            }

            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);

            dbManager.logFileOperation("move", sourcePath + " -> " + destPath, fileSize, true, null);
            return new FileOperationResult(true, "Moved successfully", fileSize, null);

        } catch (IOException e) {
            dbManager.logFileOperation("move", sourcePath + " -> " + destPath, fileSize, false, e.getMessage());
            return new FileOperationResult(false, "Failed to move: " + e.getMessage(), fileSize, e);
        }
    }

    /**
     * Get file information
     */
    public FileInfo getFileInfo(String filePath) {
        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path)) {
                return null;
            }

            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            String lastModified = attrs.lastModifiedTime().toString().substring(0, 19);

            return new FileInfo(
                path.getFileName().toString(),
                filePath,
                Files.isDirectory(path),
                attrs.size(),
                lastModified,
                Files.isReadable(path),
                Files.isWritable(path)
            );

        } catch (IOException e) {
            System.err.println("Error getting file info: " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if path exists
     */
    public boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * Get user home directory
     */
    public String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * Get application data directory
     */
    public String getAppDataDirectory() {
        return Paths.get(getUserHome(), ".java-webview-app").toString();
    }

    /**
     * Get current working directory
     */
    public String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
}
