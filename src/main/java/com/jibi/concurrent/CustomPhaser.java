package com.jibi.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CustomPhaser {

    private AtomicInteger registrationCount = new AtomicInteger(0);
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();

    private static CustomPhaser customPhaser = null;

    private CustomPhaser() {
    }

    public static CustomPhaser getInstance() {
        if (customPhaser == null) {
            customPhaser = new CustomPhaser();
        }
        return customPhaser;
    }

    public void register() {
        try {
            lock.lock();
            registrationCount.incrementAndGet();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitDeregisterAll() {
        try {
            lock.lock();
            while (registrationCount.get() > 0) {
                condition.await();
            }
            condition.signalAll();
        } catch (InterruptedException interruptedException) {
            log.warn("Interrupted", interruptedException);
        } finally {
            lock.unlock();
        }
    }

    public int getRegisteredParties() {
        try {
            lock.lock();
            condition.signalAll();
            return registrationCount.get();
        } finally {
            lock.unlock();
        }
    }

    public void deregister() {
        try {
            lock.lock();
            registrationCount.decrementAndGet();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
