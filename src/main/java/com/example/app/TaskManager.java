package com.example.app;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Task manager for handling background operations
 * Provides progress tracking, cancellation, and real-time updates
 */
public class TaskManager {
    private static TaskManager instance;
    private final Map<String, Task> activeTasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setName("TaskManager-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    private TaskManager() {
        // Start cleanup task
        ScheduledExecutorService cleanupService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TaskCleanup");
            t.setDaemon(true);
            return t;
        });
        cleanupService.scheduleAtFixedRate(this::cleanupCompletedTasks, 30, 30, TimeUnit.SECONDS);
    }

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    /**
     * Submit a new task
     */
    public String submitTask(String name, String description, Runnable task) {
        return submitTask(name, description, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Submit a new task with return value
     */
    public <T> String submitTask(String name, String description, Callable<T> task) {
        String taskId = generateTaskId();
        Task<T> taskObj = new Task<>(taskId, name, description, task);

        activeTasks.put(taskId, taskObj);
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                taskObj.setStatus(TaskStatus.RUNNING);
                T result = task.call();
                taskObj.setStatus(TaskStatus.COMPLETED);
                taskObj.setResult(result);
                return result;
            } catch (Exception e) {
                taskObj.setStatus(TaskStatus.FAILED);
                taskObj.setError(e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);

        taskObj.setFuture(future);

        // Log task creation
        DatabaseManager.getInstance().logApiCall("POST", "/api/tasks/submit", 200, 0L, true);

        // Notify via WebSocket
        WebSocketHandler.broadcastTaskUpdate(taskObj.toMap());

        return taskId;
    }

    /**
     * Submit a task with progress tracking
     */
    public String submitProgressTask(String name, String description, ProgressCallable<?> task) {
        String taskId = generateTaskId();
        Task<?> taskObj = new Task<>(taskId, name, description, task);

        activeTasks.put(taskId, taskObj);
        CompletableFuture<?> future = CompletableFuture.supplyAsync(() -> {
            try {
                taskObj.setStatus(TaskStatus.RUNNING);
                Object result = task.call(progress -> {
                    taskObj.setProgress(progress);
                    WebSocketHandler.broadcastTaskUpdate(taskObj.toMap());
                });
                taskObj.setResult(result);
                taskObj.setStatus(TaskStatus.COMPLETED);
                return result;
            } catch (Exception e) {
                taskObj.setStatus(TaskStatus.FAILED);
                taskObj.setError(e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);

        taskObj.setFuture(future);

        // Log task creation
        DatabaseManager.getInstance().logApiCall("POST", "/api/tasks/submit", 200, 0L, true);

        // Notify via WebSocket
        WebSocketHandler.broadcastTaskUpdate(taskObj.toMap());

        return taskId;
    }

    /**
     * Get task status
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        Task<?> task = activeTasks.get(taskId);
        if (task != null) {
            return task.toMap();
        }

        // Check if task exists in database (completed tasks)
        return Map.of(
            "taskId", taskId,
            "status", "NOT_FOUND",
            "message", "Task not found"
        );
    }

    public List<Map<String, Object>> getActiveTasks() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Task<?> task : activeTasks.values()) {
            result.add(task.toMap());
        }
        return result;
    }

    /**
     * Cancel a task
     */
    public boolean cancelTask(String taskId) {
        Task<?> task = activeTasks.get(taskId);
        if (task != null && task.getFuture() != null) {
            boolean cancelled = task.getFuture().cancel(true);
            if (cancelled) {
                task.setStatus(TaskStatus.CANCELLED);
                WebSocketHandler.broadcastTaskUpdate(task.toMap());

                // Log cancellation
                DatabaseManager.getInstance().logApiCall("POST", "/api/tasks/" + taskId + "/cancel", 200, 0L, true);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * Get task result (blocking)
     */
    public Object getTaskResult(String taskId) throws ExecutionException, InterruptedException, TimeoutException {
        Task<?> task = activeTasks.get(taskId);
        if (task != null && task.getFuture() != null) {
            return task.getFuture().get(30, TimeUnit.SECONDS);
        }
        throw new IllegalArgumentException("Task not found: " + taskId);
    }

    /**
     * Get task result with timeout
     */
    public Object getTaskResult(String taskId, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        Task<?> task = activeTasks.get(taskId);
        if (task != null && task.getFuture() != null) {
            return task.getFuture().get(timeout, unit);
        }
        throw new IllegalArgumentException("Task not found: " + taskId);
    }

    /**
     * Remove completed task
     */
    public boolean removeTask(String taskId) {
        Task<?> task = activeTasks.remove(taskId);
        if (task != null) {
            DatabaseManager.getInstance().logApiCall("DELETE", "/api/tasks/" + taskId, 200, 0L, true);
            return true;
        }
        return false;
    }

    /**
     * Get task statistics
     */
    public Map<String, Object> getTaskStatistics() {
        int total = activeTasks.size();
        long running = activeTasks.values().stream().mapToLong(t -> t.getStatus() == TaskStatus.RUNNING ? 1 : 0).sum();
        long completed = activeTasks.values().stream().mapToLong(t -> t.getStatus() == TaskStatus.COMPLETED ? 1 : 0).sum();
        long failed = activeTasks.values().stream().mapToLong(t -> t.getStatus() == TaskStatus.FAILED ? 1 : 0).sum();
        long cancelled = activeTasks.values().stream().mapToLong(t -> t.getStatus() == TaskStatus.CANCELLED ? 1 : 0).sum();

        return Map.of(
            "total", total,
            "running", running,
            "completed", completed,
            "failed", failed,
            "cancelled", cancelled
        );
    }

    /**
     * Shutdown task manager
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String generateTaskId() {
        return "task-" + System.currentTimeMillis() + "-" + taskCounter.incrementAndGet();
    }

    private void cleanupCompletedTasks() {
        List<String> toRemove = activeTasks.entrySet().stream()
                .filter(entry -> {
                    TaskStatus status = entry.getValue().getStatus();
                    return status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.CANCELLED;
                })
                .filter(entry -> {
                    // Remove tasks that completed more than 5 minutes ago
                    long completedAt = entry.getValue().getCompletedAt();
                    return completedAt > 0 && (System.currentTimeMillis() - completedAt) > (5 * 60 * 1000);
                })
                .map(Map.Entry::getKey)
                .toList();

        toRemove.forEach(taskId -> {
            activeTasks.remove(taskId);
            System.out.println("Cleaned up completed task: " + taskId);
        });
    }

    // Task status enum
    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    // Progress callable interface
    @FunctionalInterface
    public interface ProgressCallable<T> {
        T call(Consumer<Double> progressCallback) throws Exception;
    }

    // Task class
    private static class Task<T> {
        private final String id;
        private final String name;
        private final String description;
        private final long createdAt;
        private volatile TaskStatus status;
        private volatile double progress;
        private volatile String error;
        private volatile Object result;
        private volatile long completedAt;
        private CompletableFuture<?> future;

        public Task(String id, String name, String description, Callable<T> callable) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.createdAt = System.currentTimeMillis();
            this.status = TaskStatus.PENDING;
            this.progress = 0.0;
        }

        public Task(String id, String name, String description, ProgressCallable<?> callable) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.createdAt = System.currentTimeMillis();
            this.status = TaskStatus.PENDING;
            this.progress = 0.0;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public TaskStatus getStatus() { return status; }
        public double getProgress() { return progress; }
        public String getError() { return error; }
        public Object getResult() { return result; }
        public long getCreatedAt() { return createdAt; }
        public long getCompletedAt() { return completedAt; }
        public CompletableFuture<?> getFuture() { return future; }

        public void setStatus(TaskStatus status) {
            this.status = status;
            if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.CANCELLED) {
                this.completedAt = System.currentTimeMillis();
            }
        }

        public void setProgress(double progress) {
            this.progress = Math.max(0.0, Math.min(100.0, progress));
        }

        public void setError(String error) { this.error = error; }
        public void setResult(Object result) { this.result = result; }
        public void setFuture(CompletableFuture<?> future) { this.future = future; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", id);
            map.put("name", name);
            map.put("description", description);
            map.put("status", status.toString());
            map.put("progress", progress);
            map.put("createdAt", createdAt);
            if (completedAt > 0) {
                map.put("completedAt", completedAt);
            }
            if (error != null) {
                map.put("error", error);
            }
            if (result != null) {
                map.put("result", result.toString());
            }
            return map;
        }
    }
}