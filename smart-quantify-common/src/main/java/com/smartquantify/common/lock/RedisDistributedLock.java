package com.smartquantify.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock implements DistributedLock {

    private final RedissonClient redissonClient;
    private final Map<String, RLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
        if (acquired) {
            lockMap.put(lockKey, lock);
            log.debug("Redis lock acquired: {}", lockKey);
        }
        return acquired;
    }

    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);
        lockMap.put(lockKey, lock);
        log.debug("Redis lock locked: {}", lockKey);
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = lockMap.remove(lockKey);
        if (lock != null) {
            try {
                lock.unlock();
                log.debug("Redis lock released: {}", lockKey);
            } catch (Exception e) {
                log.warn("Failed to unlock Redis lock: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
}
