package com.example.redis.controller;

import com.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * REST Controller for Redis operations
 */
@RestController
@RequestMapping("/api/redis")
@CrossOrigin(origins = "*")
public class RedisController {

    @Autowired
    private RedisService redisService;

    // ===== STRING OPERATIONS =====

    @PostMapping("/string")
    public ResponseEntity<Map<String, Object>> setString(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) Long ttl) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (ttl != null && ttl > 0) {
                redisService.setString(key, value, ttl, TimeUnit.SECONDS);
                response.put("message", "String set with TTL: " + ttl + " seconds");
            } else {
                redisService.setString(key, value);
                response.put("message", "String set successfully");
            }
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/string/{key}")
    public ResponseEntity<Map<String, Object>> getString(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            String value = redisService.getString(key);
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            response.put("exists", value != null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== OBJECT OPERATIONS =====

    @PostMapping("/object")
    public ResponseEntity<Map<String, Object>> setObject(
            @RequestParam String key,
            @RequestBody Object value,
            @RequestParam(required = false) Long ttl) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (ttl != null && ttl > 0) {
                redisService.setObject(key, value, ttl, TimeUnit.SECONDS);
                response.put("message", "Object set with TTL: " + ttl + " seconds");
            } else {
                redisService.setObject(key, value);
                response.put("message", "Object set successfully");
            }
            response.put("success", true);
            response.put("key", key);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/object/{key}")
    public ResponseEntity<Map<String, Object>> getObject(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object value = redisService.getObject(key);
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            response.put("exists", value != null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== KEY OPERATIONS =====

    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> getKeys(
            @RequestParam(defaultValue = "*") String pattern) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Set<String> keys = redisService.getKeys(pattern);
            response.put("success", true);
            response.put("keys", keys);
            response.put("count", keys.size());
            response.put("pattern", pattern);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/key/{key}")
    public ResponseEntity<Map<String, Object>> deleteKey(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = redisService.deleteKey(key);
            response.put("success", true);
            response.put("deleted", deleted);
            response.put("key", key);
            response.put("message", deleted ? "Key deleted successfully" : "Key not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/key/{key}/exists")
    public ResponseEntity<Map<String, Object>> keyExists(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = redisService.hasKey(key);
            response.put("success", true);
            response.put("key", key);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/key/{key}/ttl")
    public ResponseEntity<Map<String, Object>> getTTL(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            long ttl = redisService.getTTL(key);
            response.put("success", true);
            response.put("key", key);
            response.put("ttl", ttl);
            response.put("message", ttl == -1 ? "No expiration" : ttl == -2 ? "Key not found" : "TTL: " + ttl + " seconds");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== HASH OPERATIONS =====

    @PostMapping("/hash/{key}")
    public ResponseEntity<Map<String, Object>> setHashField(
            @PathVariable String key,
            @RequestParam String field,
            @RequestBody Object value) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            redisService.setHashField(key, field, value);
            response.put("success", true);
            response.put("message", "Hash field set successfully");
            response.put("key", key);
            response.put("field", field);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/hash/{key}/{field}")
    public ResponseEntity<Map<String, Object>> getHashField(
            @PathVariable String key,
            @PathVariable String field) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Object value = redisService.getHashField(key, field);
            response.put("success", true);
            response.put("key", key);
            response.put("field", field);
            response.put("value", value);
            response.put("exists", value != null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/hash/{key}")
    public ResponseEntity<Map<String, Object>> getAllHashFields(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<Object, Object> fields = redisService.getAllHashFields(key);
            response.put("success", true);
            response.put("key", key);
            response.put("fields", fields);
            response.put("count", fields.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== LIST OPERATIONS =====

    @PostMapping("/list/{key}/push")
    public ResponseEntity<Map<String, Object>> pushToList(
            @PathVariable String key,
            @RequestBody Object value,
            @RequestParam(defaultValue = "right") String direction) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            long size = direction.equalsIgnoreCase("left") ? 
                redisService.leftPush(key, value) : 
                redisService.rightPush(key, value);
            
            response.put("success", true);
            response.put("message", "Value pushed to " + direction + " of list");
            response.put("key", key);
            response.put("newSize", size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list/{key}/pop")
    public ResponseEntity<Map<String, Object>> popFromList(
            @PathVariable String key,
            @RequestParam(defaultValue = "right") String direction) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Object value = direction.equalsIgnoreCase("left") ? 
                redisService.leftPop(key) : 
                redisService.rightPop(key);
            
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            response.put("message", value != null ? "Value popped from " + direction : "List is empty");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list/{key}")
    public ResponseEntity<Map<String, Object>> getList(
            @PathVariable String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<Object> list = redisService.getListRange(key, start, end);
            long size = redisService.getListSize(key);
            
            response.put("success", true);
            response.put("key", key);
            response.put("list", list);
            response.put("totalSize", size);
            response.put("range", start + " to " + end);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== UTILITY OPERATIONS =====

    @PostMapping("/increment/{key}")
    public ResponseEntity<Map<String, Object>> increment(
            @PathVariable String key,
            @RequestParam(defaultValue = "1") long delta) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            long value = delta == 1 ? 
                redisService.increment(key) : 
                redisService.increment(key, delta);
            
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            response.put("delta", delta);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> info = redisService.getRedisInfo();
            response.put("success", true);
            response.put("info", info);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, Object>> flushDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Note: This is a dangerous operation - use with caution
            response.put("success", false);
            response.put("message", "Flush operation disabled for safety. Enable in code if needed.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}