package org.example;

import java.util.concurrent.*;

/**
 * EXAMPLE 3: EXECUTORSERVICE & THREAD POOL
 *
 * Managing multiple threads manually is complex. ExecutorService provides
 * a higher-level abstraction for thread pool management.
 *
 * HIGH-LEVEL CONCEPTS:
 * - Thread Pool: Pre-created threads that wait for tasks (more efficient than creating new threads)
 * - ExecutorService: Manages thread pool and task submission
 * - Benefits: Reduced thread creation overhead, controlled resource usage, better performance
 * - Types: Fixed Pool, Cached Pool, Single Thread, Scheduled
 */

public class ExecutorServiceExample {

    // ==================== SIMPLE TASK ====================

    static class SimpleTask implements Runnable {
        private String taskId;

        public SimpleTask(String id) {
            this.taskId = id;
        }

        @Override
        public void run() {
            System.out.println("Task " + taskId + " started [Thread: " +
                Thread.currentThread().getName() + "]");

            try {
                // Simulate work
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Task " + taskId + " was interrupted");
                Thread.currentThread().interrupt();
            }

            System.out.println("Task " + taskId + " completed");
        }
    }

    public static void demonstrate() throws InterruptedException {

        // ==================== 1. FIXED THREAD POOL ====================

        System.out.println("\n>>> 1. FIXED THREAD POOL (newFixedThreadPool)");
        System.out.println("Creates a pool with fixed number of threads.");
        System.out.println("Queue stores waiting tasks if all threads are busy.\n");

        // Create a pool with 3 threads
        ExecutorService executor1 = Executors.newFixedThreadPool(3);

        System.out.println("Submitting 10 tasks to pool with 3 threads:");
        for (int i = 1; i <= 10; i++) {
            executor1.execute(new SimpleTask("FIX-" + i));
        }

        System.out.println("All tasks submitted. Waiting for completion...");

        // Shutdown the executor (no new tasks accepted)
        executor1.shutdown();

        // Wait for all tasks to complete (with timeout)
        if (!executor1.awaitTermination(15, TimeUnit.SECONDS)) {
            System.out.println("Tasks did not finish within timeout!");
            executor1.shutdownNow(); // Force shutdown
        }

        // ==================== 2. CACHED THREAD POOL ====================

        System.out.println("\n\n>>> 2. CACHED THREAD POOL (newCachedThreadPool)");
        System.out.println("Creates threads as needed, reuses idle threads.");
        System.out.println("Good for many short-lived tasks.\n");

        ExecutorService executor2 = Executors.newCachedThreadPool();

        System.out.println("Submitting 5 short tasks:");
        for (int i = 1; i <= 5; i++) {
            executor2.execute(new SimpleTask("CACHE-" + i));
        }

        executor2.shutdown();
        executor2.awaitTermination(15, TimeUnit.SECONDS);

        // ==================== 3. SINGLE THREAD EXECUTOR ====================

        System.out.println("\n\n>>> 3. SINGLE THREAD EXECUTOR (newSingleThreadExecutor)");
        System.out.println("Only one thread executes tasks sequentially.");
        System.out.println("Useful when ordering of tasks matters.\n");

        ExecutorService executor3 = Executors.newSingleThreadExecutor();

        System.out.println("Submitting 5 tasks (will execute sequentially):");
        for (int i = 1; i <= 5; i++) {
            executor3.execute(new SimpleTask("SEQ-" + i));
        }

        executor3.shutdown();
        executor3.awaitTermination(15, TimeUnit.SECONDS);

        // ==================== 4. SCHEDULED THREAD POOL ====================

        System.out.println("\n\n>>> 4. SCHEDULED THREAD POOL (newScheduledThreadPool)");
        System.out.println("Executes tasks after delay or periodically.\n");

        ScheduledExecutorService executor4 = Executors.newScheduledThreadPool(2);

        // Schedule task to run once after 2 seconds delay
        System.out.println("Scheduling task to run after 2 seconds delay...");
        executor4.schedule(
            new SimpleTask("DELAYED-1"),
            2,
            TimeUnit.SECONDS
        );

        // Schedule task to run repeatedly every 1.5 seconds, starting after 0.5 seconds
        System.out.println("Scheduling task to run periodically every 1.5 seconds...");
        ScheduledFuture<?> future = executor4.scheduleAtFixedRate(
            () -> {
                System.out.println("  Periodic task running [Thread: " +
                    Thread.currentThread().getName() + "]");
            },
            500,      // Initial delay in milliseconds
            1500,     // Period between executions
            TimeUnit.MILLISECONDS
        );

        // Let it run for about 5 seconds
        Thread.sleep(5000);
        future.cancel(false); // Cancel the periodic task

        executor4.shutdown();
        executor4.awaitTermination(10, TimeUnit.SECONDS);

        // ==================== 5. SUBMIT TASKS WITH RETURN VALUE ====================

        System.out.println("\n\n>>> 5. SUBMIT TASKS AND GET RETURN VALUE");
        System.out.println("Using submit() instead of execute() for tasks with return value.\n");

        ExecutorService executor5 = Executors.newFixedThreadPool(2);

        // submit() returns a Future object
        Future<Integer> result1 = executor5.submit(() -> {
            System.out.println("  Computing task 1 [Thread: " +
                Thread.currentThread().getName() + "]");
            Thread.sleep(1000);
            return 100;
        });

        Future<Integer> result2 = executor5.submit(() -> {
            System.out.println("  Computing task 2 [Thread: " +
                Thread.currentThread().getName() + "]");
            Thread.sleep(800);
            return 200;
        });

        System.out.println("Tasks submitted, retrieving results...");

        try {
            // get() blocks until result is available
            int value1 = result1.get();
            int value2 = result2.get();

            System.out.println("Task 1 returned: " + value1);
            System.out.println("Task 2 returned: " + value2);
            System.out.println("Sum: " + (value1 + value2));

        } catch (ExecutionException e) {
            System.out.println("Task execution failed: " + e.getCause());
        }

        executor5.shutdown();
        executor5.awaitTermination(10, TimeUnit.SECONDS);

        // ==================== 6. INVOKEALL - RUN ALL TASKS AND COLLECT RESULTS ====================

        System.out.println("\n\n>>> 6. INVOKEALL - SUBMIT MULTIPLE TASKS AND WAIT FOR ALL");
        System.out.println("Useful when you need results from all tasks.\n");

        ExecutorService executor6 = Executors.newFixedThreadPool(3);

        java.util.List<java.util.concurrent.Callable<Integer>> tasks = new java.util.ArrayList<>();
        tasks.add(() -> { Thread.sleep(500); return 10; });
        tasks.add(() -> { Thread.sleep(800); return 20; });
        tasks.add(() -> { Thread.sleep(600); return 30; });

        System.out.println("Submitting 3 tasks with invokeAll()");
        java.util.List<Future<Integer>> futures = executor6.invokeAll(tasks);

        System.out.println("All tasks completed. Collecting results:");
        int sum = 0;
        for (Future<Integer> future : futures) {
            int value = future.get();
            System.out.println("  Result: " + value);
            sum += value;
        }
        System.out.println("Total sum: " + sum);

        executor6.shutdown();
        executor6.awaitTermination(10, TimeUnit.SECONDS);
    }
}

