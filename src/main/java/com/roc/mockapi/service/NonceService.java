package com.roc.mockapi.service;

import com.roc.mockapi.ApiSignConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 随机数服务，用于API调用防重放
 *
 * @author lipeng
 * @since 2026/2/26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NonceService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * nonce 在 Redis 中的 key 前缀
     */
    private static final String NONCE_KEY_PREFIX = "api:nonce:";

    /**
     * Lua 脚本：原子性地检查 nonce 是否存在，不存在则设置
     * 返回 1 表示成功（nonce 不存在，已记录）
     * 返回 0 表示失败（nonce 已存在，重复请求）
     */
    private static final String NONCE_LUA_SCRIPT = """
            if redis.call('exists', KEYS[1]) == 1 then
                return 0  -- nonce 已存在，拒绝
            end
            redis.call('setex', KEYS[1], ARGV[1], 1)  -- 设置 nonce，TTL 为 ARGV[1] 秒
            return 1  -- 成功
            """;

    /**
     * 验证 nonce 并记录（原子操作）
     *
     * @param nonce 随机数
     * @return true 表示 nonce 有效且未被使用过；false 表示重复请求
     */
    public boolean verifyAndRecordNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            log.warn("Nonce is null or empty");
            return false;
        }

        String key = NONCE_KEY_PREFIX + nonce;
        long ttlSeconds = ApiSignConstant.REQUEST_VALID_MINUTES * 60;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(NONCE_LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(ttlSeconds));

        boolean valid = result != null && result == 1L;
        if (!valid) {
            log.warn("Duplicate request detected, nonce: {}", nonce);
        }
        return valid;
    }

    /**
     * 手动移除 nonce（一般不需要，TTL 会自动过期）
     */
    public void removeNonce(String nonce) {
        String key = NONCE_KEY_PREFIX + nonce;
        redisTemplate.delete(key);
    }
}
