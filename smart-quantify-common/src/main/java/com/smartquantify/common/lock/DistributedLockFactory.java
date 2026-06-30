package com.smartquantify.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DistributedLockFactory {

    private final RedisDistributedLock redisLock;
    private final EtcdDistributedLock etcdLock;
    private final String lockMode;

    public DistributedLockFactory(RedisDistributedLock redisLock, EtcdDistributedLock etcdLock,
                                  @Value("${smartquantify.lock.mode:redis}") String lockMode) {
        this.redisLock = redisLock;
        this.etcdLock = etcdLock;
        this.lockMode = lockMode;
        log.info("Distributed lock mode: {}", lockMode);
    }

    public DistributedLock getLock() {
        return "etcd".equalsIgnoreCase(lockMode) ? etcdLock : redisLock;
    }

    public DistributedLock getLock(String mode) {
        return "etcd".equalsIgnoreCase(mode) ? etcdLock : redisLock;
    }
}
