package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EXAMPLE 2: THREAD SYNCHRONIZATION & MUTUAL EXCLUSION
 *
 * When multiple threads access shared resources, we need synchronization.
 *
 * HIGH-LEVEL CONCEPTS:
 * - Race Condition: Multiple threads accessing/modifying shared data without coordination
 * - Critical Section: Code that accesses shared resources and must be protected
 * - Mutual Exclusion: Only one thread can execute critical section at a time
 * - Lock/Monitor: Mechanism to enforce mutual exclusion
 */

public class ThreadSynchronizationExample {

    // ==================== PROBLEM: RACE CONDITION ====================

    /**
     * Shared counter that multiple threads will increment
     * This demonstrates the race condition problem
     */
    static class UnsafeCounter {
        private int count = 0;

        // UNSAFE: Multiple threads can access/modify count simultaneously
        public void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    // ==================== SOLUTION 1: SYNCHRONIZED METHOD ====================

    /**
     * Using synchronized method - simplest approach
     * Each object has an intrinsic lock (monitor)
     * Only one thread can execute synchronized method at a time
     */
    static class SafeCounterWithSynchronizedMethod {
        private int count = 0;

        // Synchronized method acquires the object's lock
        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    // ==================== SOLUTION 2: SYNCHRONIZED BLOCK ====================

    /**
     * Synchronized block allows finer control
     * You specify which object's lock to acquire
     * More efficient than synchronizing entire method
     */
    static class SafeCounterWithSynchronizedBlock {
        private int count = 0;

        public void increment() {
            // Only the critical section is synchronized
            synchronized (this) {
                count++;
            }
        }

        public int getCount() {
            synchronized (this) {
                return count;
            }
        }
    }

    // ==================== SOLUTION 3: REENTRANT LOCK ====================

    /**
     * Using ReentrantLock from java.util.concurrent.locks
     * More powerful than synchronized:
     * - Can check if lock is available without blocking (tryLock)
     * - Can set timeout for acquiring lock
     * - Can create multiple conditions for signaling
     * - Same thread can acquire lock multiple times (reentrant)
     */
    static class SafeCounterWithLock {
        private int count = 0;
        private final Lock lock = new ReentrantLock();

        public void increment() {
            lock.lock(); // Acquire lock
            try {
                count++;
            } finally {
                lock.unlock(); // Always release lock in finally
            }
        }

        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    // ==================== PRODUCER-CONSUMER WITH WAIT/NOTIFY ====================

    /**
     * Demonstrates inter-thread communication
     * Threads can wait for conditions and notify each other
     */
    static class Buffer {
        private int value;
        private boolean isEmpty = true;

        // Producer thread calls this to put value
        public synchronized void put(int newValue) {
            // Wait while buffer is full (not empty)
            while (!isEmpty) {
                try {
                    System.out.println("  Producer waiting - buffer full");
                    wait(); // Release lock and wait
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            value = newValue;
            isEmpty = false;
            System.out.println("  Producer produced: " + value);

            // Notify consumer that value is ready
            notifyAll();
        }

        // Consumer thread calls this to get value
        public synchronized int get() {
            // Wait while buffer is empty
            while (isEmpty) {
                try {
                    System.out.println("  Consumer waiting - buffer empty");
                    wait(); // Release lock and wait
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            isEmpty = true;
            System.out.println("  Consumer consumed: " + value);

            // Notify producer that buffer is empty
            notifyAll();
            return value;
        }
    }

    public static void demonstrate() throws InterruptedException {

        // ==================== DEMONSTRATE RACE CONDITION ====================

        System.out.println("\n>>> PROBLEM: Race Condition (Unsafe Counter)");
        System.out.println("Creating 5 threads, each incrementing counter 1000 times");
        System.out.println("Expected final value: 5000\n");

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        Thread[] threads1 = new Thread[5];

        for (int i = 0; i < 5; i++) {
            threads1[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter.increment();
                }
            }, "UnsafeThread-" + i);
            threads1[i].start();
        }

        for (Thread t : threads1) {
            t.join();
        }

        System.out.println("Final unsafe count: " + unsafeCounter.getCount());
        System.out.println("NOTICE: It's likely NOT 5000! (Race condition occurred)");

        // ==================== DEMONSTRATE SYNCHRONIZED METHOD ====================

        System.out.println("\n>>> SOLUTION 1: Synchronized Method");
        System.out.println("Same test with synchronized method\n");

        SafeCounterWithSynchronizedMethod safeCounter1 = new SafeCounterWithSynchronizedMethod();
        Thread[] threads2 = new Thread[5];

        for (int i = 0; i < 5; i++) {
            threads2[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    safeCounter1.increment();
                }
            }, "SyncThread-" + i);
            threads2[i].start();
        }

        for (Thread t : threads2) {
            t.join();
        }

        System.out.println("Final synchronized count: " + safeCounter1.getCount());
        System.out.println("CORRECT! It's always 5000 (mutual exclusion enforced)");

        // ==================== DEMONSTRATE REENTRANT LOCK ====================

        System.out.println("\n>>> SOLUTION 3: ReentrantLock");
        System.out.println("Same test using ReentrantLock\n");

        SafeCounterWithLock safeCounter3 = new SafeCounterWithLock();
        Thread[] threads3 = new Thread[5];

        for (int i = 0; i < 5; i++) {
            threads3[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    safeCounter3.increment();
                }
            }, "LockThread-" + i);
            threads3[i].start();
        }

        for (Thread t : threads3) {
            t.join();
        }

        System.out.println("Final lock-based count: " + safeCounter3.getCount());
        System.out.println("CORRECT! ReentrantLock ensures thread safety");

        // ==================== DEMONSTRATE WAIT/NOTIFY ====================

        System.out.println("\n>>> PRODUCER-CONSUMER PATTERN WITH WAIT/NOTIFY");
        System.out.println("One thread produces, one thread consumes\n");

        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                buffer.put(i * 10);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                buffer.get();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("\nProducer-Consumer pattern completed successfully!");
    }
}

