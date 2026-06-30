package com.smartquantify.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EtcdDistributedLock implements DistributedLock {

    private final Map<String, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        AtomicBoolean lock = lockMap.computeIfAbsent(lockKey, k -> new AtomicBoolean(false));
        
        long startTime = System.currentTimeMillis();
        long waitMs = unit.toMillis(waitTime);
        
        while (System.currentTimeMillis() - startTime < waitMs) {
            if (lock.compareAndSet(false, true)) {
                log.debug("Etcd lock acquired: {}", lockKey);
                scheduleRelease(lockKey, leaseTime, unit);
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit unit) {
        AtomicBoolean lock = lockMap.computeIfAbsent(lockKey, k -> new AtomicBoolean(false));
        while (!lock.compareAndSet(false, true)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted");
            }
        }
        log.debug("Etcd lock locked: {}", lockKey);
        scheduleRelease(lockKey, leaseTime, unit);
    }

    private void scheduleRelease(String lockKey, long leaseTime, TimeUnit unit) {
        new Thread(() -> {
            try {
                Thread.sleep(unit.toMillis(leaseTime));
                unlock(lockKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "etcd-lock-release-" + lockKey).start();
    }

    @Override
    public void unlock(String lockKey) {
        AtomicBoolean lock = lockMap.get(lockKey);
        if (lock != null) {
            lock.set(false);
            log.debug("Etcd lock released: {}", lockKey);
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        AtomicBoolean lock = lockMap.get(lockKey);
        return lock != null && lock.get();
    }
}
