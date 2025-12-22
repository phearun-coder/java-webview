package com.example.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Service for all Redis operations
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ===== STRING OPERATIONS =====

    /**
     * Set a string value
     */
    public void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set a string value with expiration
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Get a string value
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Set an object value
     */
    public void setObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set an object value with expiration
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Get an object value
     */
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ===== KEY OPERATIONS =====

    /**
     * Check if key exists
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Delete a key
     */
    public boolean deleteKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * Delete multiple keys
     */
    public long deleteKeys(Collection<String> keys) {
        Long deleted = redisTemplate.delete(keys);
        return deleted != null ? deleted : 0;
    }

    /**
     * Set expiration for a key
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * Get TTL for a key
     */
    public long getTTL(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : -1;
    }

    /**
     * Get all keys matching a pattern
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // ===== HASH OPERATIONS =====

    /**
     * Set hash field
     */
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Get hash field
     */
    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Set multiple hash fields
     */
    public void setHashFields(String key, Map<String, Object> fields) {
        redisTemplate.opsForHash().putAll(key, fields);
    }

    /**
     * Get all hash fields
     */
    public Map<Object, Object> getAllHashFields(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Delete hash field
     */
    public boolean deleteHashField(String key, String field) {
        Long deleted = redisTemplate.opsForHash().delete(key, field);
        return deleted != null && deleted > 0;
    }

    // ===== LIST OPERATIONS =====

    /**
     * Push to left of list
     */
    public long leftPush(String key, Object value) {
        Long size = redisTemplate.opsForList().leftPush(key, value);
        return size != null ? size : 0;
    }

    /**
     * Push to right of list
     */
    public long rightPush(String key, Object value) {
        Long size = redisTemplate.opsForList().rightPush(key, value);
        return size != null ? size : 0;
    }

    /**
     * Pop from left of list
     */
    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * Pop from right of list
     */
    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * Get list range
     */
    public List<Object> getListRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Get list size
     */
    public long getListSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    // ===== SET OPERATIONS =====

    /**
     * Add to set
     */
    public long addToSet(String key, Object... values) {
        Long added = redisTemplate.opsForSet().add(key, values);
        return added != null ? added : 0;
    }

    /**
     * Get all set members
     */
    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Check if member exists in set
     */
    public boolean isSetMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * Remove from set
     */
    public long removeFromSet(String key, Object... values) {
        Long removed = redisTemplate.opsForSet().remove(key, values);
        return removed != null ? removed : 0;
    }

    /**
     * Get set size
     */
    public long getSetSize(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

    // ===== SORTED SET OPERATIONS =====

    /**
     * Add to sorted set
     */
    public boolean addToSortedSet(String key, Object value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
    }

    /**
     * Get sorted set range
     */
    public Set<Object> getSortedSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Get sorted set range by score
     */
    public Set<Object> getSortedSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    // ===== UTILITY OPERATIONS =====

    /**
     * Increment a numeric value
     */
    public long increment(String key) {
        Long result = stringRedisTemplate.opsForValue().increment(key);
        return result != null ? result : 0;
    }

    /**
     * Increment by a specific amount
     */
    public long increment(String key, long delta) {
        Long result = stringRedisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /**
     * Decrement a numeric value
     */
    public long decrement(String key) {
        Long result = stringRedisTemplate.opsForValue().decrement(key);
        return result != null ? result : 0;
    }

    /**
     * Get Redis info
     */
    public Map<String, String> getRedisInfo() {
        Map<String, String> info = new HashMap<>();
        try {
            // Get some basic info
            info.put("connection", "Connected");
            info.put("dbSize", String.valueOf(redisTemplate.getConnectionFactory()
                .getConnection().dbSize()));
            info.put("timestamp", new Date().toString());
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        return info;
    }
}