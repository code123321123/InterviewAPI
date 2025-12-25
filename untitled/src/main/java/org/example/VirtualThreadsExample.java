package org.example;

import java.util.concurrent.*;

/**
 * EXAMPLE 6: VIRTUAL THREADS (Java 19+)
 *
 * Virtual Threads (Project Loom) are lightweight threads managed by JVM.
 * They enable writing scalable concurrent applications with millions of threads.
 *
 * HIGH-LEVEL CONCEPTS:
 * - Virtual Thread: Lightweight managed by JVM, not 1:1 with OS threads
 * - Platform Thread: Traditional OS thread (expensive, limited count)
 * - Carrier Thread: Platform thread that carries virtual threads
 * - Benefits: Create millions of virtual threads vs thousands of platform threads
 * - Syntax: Thread.ofVirtual() or Thread.startVirtualThread()
 *
 * WHY VIRTUAL THREADS ARE GAME-CHANGING:
 * - Extremely lightweight (low memory overhead)
 * - No need for complex async/await patterns
 * - Can write blocking code that's actually efficient
 * - Perfect for I/O-bound operations (network, database)
 */

public class VirtualThreadsExample {

    // ==================== SIMULATED I/O OPERATION ====================

    /**
     * Simulates an I/O operation (like network request)
     * Virtual threads shine here because they don't block OS resources
     */
    static String simulateIOOperation(int taskId) throws InterruptedException {
        System.out.println("  Task " + taskId + " starting I/O operation [Thread: " +
            Thread.currentThread().getName() + "]");

        // In real applications, this would be network I/O
        Thread.sleep(1000);

        System.out.println("  Task " + taskId + " I/O completed");
        return "Result-" + taskId;
    }

    public static void demonstrate() throws InterruptedException, ExecutionException {

        System.out.println("Note: Virtual Threads require Java 19+");
        System.out.println("If you're on Java 17, this example shows the syntax for Java 19+\n");

        // Check Java version
        String javaVersion = System.getProperty("java.version");
        System.out.println("Your Java version: " + javaVersion);

        boolean isJava19Plus = Integer.parseInt(javaVersion.split("\\.")[0]) >= 19;

        if (!isJava19Plus) {
            System.out.println("\nNote: Virtual Threads require Java 19+");
            System.out.println("Showing alternative approach compatible with Java 17\n");
            demonstrateWithExecutor();
            return;
        }

        demonstrateVirtualThreads();
    }

    /**
     * Demonstrates virtual threads (Java 19+)
     */
    static void demonstrateVirtualThreads() throws InterruptedException, ExecutionException {

        // ==================== 1. CREATING VIRTUAL THREADS ====================

        System.out.println("\n>>> 1. CREATING VIRTUAL THREADS");
        System.out.println("Creating and starting virtual threads\n");

        // Approach 1: Using Thread.ofVirtual()
        Thread virtualThread1 = Thread.ofVirtual()
            .name("virtual-1")
            .start(() -> {
                try {
                    System.out.println("Virtual Thread 1 running");
                    Thread.sleep(500);
                    System.out.println("Virtual Thread 1 completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

        // Approach 2: Using Thread.startVirtualThread()
        Thread virtualThread2 = Thread.startVirtualThread(() -> {
            try {
                System.out.println("Virtual Thread 2 running");
                Thread.sleep(500);
                System.out.println("Virtual Thread 2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        virtualThread1.join();
        virtualThread2.join();

        // ==================== 2. MASSIVE VIRTUAL THREAD CREATION ====================

        System.out.println("\n\n>>> 2. MASSIVE VIRTUAL THREAD CREATION");
        System.out.println("Create thousands of virtual threads easily\n");

        long startTime = System.currentTimeMillis();

        System.out.println("Creating 10,000 virtual threads...");
        Thread[] virtualThreads = new Thread[10000];

        for (int i = 0; i < 10000; i++) {
            final int taskId = i;
            virtualThreads[i] = Thread.startVirtualThread(() -> {
                try {
                    simulateIOOperation(taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.println("All threads created. Waiting for completion...");

        // Wait for all threads to complete
        for (Thread t : virtualThreads) {
            t.join();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("10,000 virtual threads completed in " + elapsed + "ms");
        System.out.println("(This would be impossible with platform threads!)");

        // ==================== 3. VIRTUAL THREADS WITH EXECUTOR ====================

        System.out.println("\n\n>>> 3. VIRTUAL THREADS WITH EXECUTOR");
        System.out.println("Using virtual threads with ExecutorService\n");

        // Create an executor that uses virtual threads
        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        System.out.println("Submitting 100 tasks to virtual thread executor...");

        startTime = System.currentTimeMillis();

        java.util.List<Future<String>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            futures.add(virtualExecutor.submit(() -> simulateIOOperation(taskId)));
        }

        // Collect all results
        int successCount = 0;
        for (Future<String> future : futures) {
            String result = future.get();
            successCount++;
        }

        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Completed " + successCount + " tasks in " + elapsed + "ms");

        virtualExecutor.shutdown();
        virtualExecutor.awaitTermination(10, TimeUnit.SECONDS);

        // ==================== 4. STRUCTURED CONCURRENCY (Preview) ====================

        System.out.println("\n\n>>> 4. STRUCTURED CONCURRENCY");
        System.out.println("Managing groups of virtual threads together\n");

        System.out.println("Creating a group of related tasks...");

        // Modern way: Using virtual threads naturally reads like sequential code
        Thread group1 = Thread.ofVirtual().start(() -> {
            try {
                System.out.println("  Subtask 1.1 started");
                Thread.sleep(500);
                System.out.println("  Subtask 1.1 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread group2 = Thread.ofVirtual().start(() -> {
            try {
                System.out.println("  Subtask 1.2 started");
                Thread.sleep(300);
                System.out.println("  Subtask 1.2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        group1.join();
        group2.join();
        System.out.println("Group tasks completed");

        // ==================== 5. COMPARISON: PLATFORM VS VIRTUAL ====================

        System.out.println("\n\n>>> 5. COMPARISON: PLATFORM VS VIRTUAL THREADS");
        System.out.println("Memory and startup overhead comparison\n");

        // Platform threads
        System.out.println("Creating 1000 platform threads...");
        startTime = System.currentTimeMillis();
        Thread[] platformThreads = new Thread[1000];

        ExecutorService platformExecutor = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            platformExecutor.submit(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        platformExecutor.shutdown();
        platformExecutor.awaitTermination(5, TimeUnit.SECONDS);

        long platformTime = System.currentTimeMillis() - startTime;
        System.out.println("Platform threads (1000) took: " + platformTime + "ms");

        // Virtual threads
        System.out.println("Creating 100,000 virtual threads...");
        startTime = System.currentTimeMillis();

        ExecutorService virtualExecutor2 = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 100000; i++) {
            virtualExecutor2.submit(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        virtualExecutor2.shutdown();
        virtualExecutor2.awaitTermination(10, TimeUnit.SECONDS);

        long virtualTime = System.currentTimeMillis() - startTime;
        System.out.println("Virtual threads (100,000) took: " + virtualTime + "ms");
        System.out.println("\nVirtual threads enable 100x more concurrency!");

        // ==================== 6. I/O-BOUND WORKLOAD ====================

        System.out.println("\n\n>>> 6. PERFECT FOR I/O-BOUND WORKLOADS");
        System.out.println("Virtual threads make blocking I/O efficient\n");

        ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();

        System.out.println("Simulating 50 concurrent I/O operations...");
        startTime = System.currentTimeMillis();

        java.util.List<CompletableFuture<String>> ioFutures = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final int taskId = i;
            ioFutures.add(CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return simulateIOOperation(taskId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                ioExecutor
            ));
        }

        CompletableFuture.allOf(
            ioFutures.toArray(new CompletableFuture[0])
        ).get();

        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("50 I/O operations completed in " + elapsed + "ms");
        System.out.println("Virtual threads execute concurrently without thread starvation!");

        ioExecutor.shutdown();
        ioExecutor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Alternative approach for Java 17
     * Shows equivalent concept using traditional tools
     */
    static void demonstrateWithExecutor() throws InterruptedException, ExecutionException {

        System.out.println("\n>>> JAVA 17 COMPATIBLE ALTERNATIVE");
        System.out.println("Using ExecutorService with many threads\n");

        // Simulate virtual thread behavior with thread pool
        ExecutorService executor = Executors.newFixedThreadPool(50);

        System.out.println("Submitting 100 tasks with thread pool (simulating virtual threads)...");

        long startTime = System.currentTimeMillis();

        java.util.List<Future<String>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            futures.add(executor.submit(() -> {
                try {
                    return simulateIOOperation(taskId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        // Collect results
        for (Future<String> future : futures) {
            future.get();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("100 tasks completed in " + elapsed + "ms");
        System.out.println("(Note: This uses 50 platform threads, virtual threads would use fewer resources)");

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}

