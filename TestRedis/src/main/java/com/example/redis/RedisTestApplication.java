package com.example.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main Spring Boot Application for Redis Testing
 */
@SpringBootApplication
public class RedisTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisTestApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 Redis Test Application Started Successfully!");
        System.out.println("📡 Server running at: http://localhost:8081/redis-app");
        System.out.println("🔧 API Base URL: http://localhost:8081/redis-app/api/redis");
        System.out.println("🌐 Test Page: http://localhost:8081/redis-app/");
        System.out.println("📊 Health Check: http://localhost:8081/redis-app/actuator/health");
        System.out.println("=".repeat(60));
        
        System.out.println("\n📋 Available API Endpoints:");
        System.out.println("┌─ String Operations:");
        System.out.println("│  POST /api/redis/string?key=mykey&value=myvalue&ttl=60");
        System.out.println("│  GET  /api/redis/string/{key}");
        System.out.println("│");
        System.out.println("┌─ Object Operations:");
        System.out.println("│  POST /api/redis/object?key=mykey&ttl=60 (JSON body)");
        System.out.println("│  GET  /api/redis/object/{key}");
        System.out.println("│");
        System.out.println("┌─ Key Operations:");
        System.out.println("│  GET    /api/redis/keys?pattern=*");
        System.out.println("│  DELETE /api/redis/key/{key}");
        System.out.println("│  GET    /api/redis/key/{key}/exists");
        System.out.println("│  GET    /api/redis/key/{key}/ttl");
        System.out.println("│");
        System.out.println("┌─ Hash Operations:");
        System.out.println("│  POST /api/redis/hash/{key}?field=myfield (JSON body)");
        System.out.println("│  GET  /api/redis/hash/{key}/{field}");
        System.out.println("│  GET  /api/redis/hash/{key}");
        System.out.println("│");
        System.out.println("┌─ List Operations:");
        System.out.println("│  POST /api/redis/list/{key}/push?direction=right (JSON body)");
        System.out.println("│  POST /api/redis/list/{key}/pop?direction=right");
        System.out.println("│  GET  /api/redis/list/{key}?start=0&end=-1");
        System.out.println("│");
        System.out.println("┌─ Utility Operations:");
        System.out.println("│  POST /api/redis/increment/{key}?delta=1");
        System.out.println("│  GET  /api/redis/info");
        System.out.println("└─");
        
        System.out.println("\n💡 Quick Test Commands:");
        System.out.println("curl -X POST \"http://localhost:8081/redis-app/api/redis/string?key=test&value=hello\"");
        System.out.println("curl \"http://localhost:8081/redis-app/api/redis/string/test\"");
        System.out.println("curl \"http://localhost:8081/redis-app/api/redis/info\"");
        System.out.println();
    }
}