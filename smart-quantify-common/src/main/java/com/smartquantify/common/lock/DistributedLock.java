package com.smartquantify.common.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {

    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    void lock(String lockKey, long leaseTime, TimeUnit unit);

    void unlock(String lockKey);

    boolean isLocked(String lockKey);
}
