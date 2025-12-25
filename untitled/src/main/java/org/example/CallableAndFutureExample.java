package org.example;

import java.util.concurrent.*;

/**
 * EXAMPLE 4: CALLABLE & FUTURE
 *
 * Runnable has limitations - it can't return a value or throw checked exceptions.
 * Callable and Future provide a more powerful abstraction.
 *
 * HIGH-LEVEL CONCEPTS:
 * - Callable: Like Runnable but can return a result and throw checked exceptions
 * - Future: Represents the result of an asynchronous computation
 *   - Can check if computation is done
 *   - Can retrieve the result (blocking)
 *   - Can cancel the computation
 * - Useful for tasks that take time and we need their results
 */

public class CallableAndFutureExample {

    // ==================== SAMPLE CALLABLE TASKS ====================

    /**
     * A Callable that returns an Integer
     */
    static class NumberGeneratorTask implements Callable<Integer> {
        private String taskName;
        private int start;
        private int end;

        public NumberGeneratorTask(String name, int start, int end) {
            this.taskName = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public Integer call() throws Exception {
            System.out.println(taskName + " started [Thread: " +
                Thread.currentThread().getName() + "]");

            // Simulate work
            Thread.sleep(2000);

            // Calculate sum from start to end
            int sum = 0;
            for (int i = start; i <= end; i++) {
                sum += i;
            }

            System.out.println(taskName + " completed. Sum(" + start + "-" + end + ") = " + sum);
            return sum;
        }
    }

    /**
     * A Callable that returns a String
     */
    static class FileProcessorTask implements Callable<String> {
        private String filename;

        public FileProcessorTask(String filename) {
            this.filename = filename;
        }

        @Override
        public String call() throws Exception {
            System.out.println("Processing file: " + filename);

            // Simulate file processing
            Thread.sleep(1500);

            String result = "Processed " + filename + " successfully";
            System.out.println(result);
            return result;
        }
    }

    /**
     * A Callable that throws a checked exception
     */
    static class FailingTask implements Callable<String> {
        private boolean shouldFail;

        public FailingTask(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public String call() throws Exception {
            if (shouldFail) {
                throw new Exception("Task intentionally failed!");
            }
            return "Task completed successfully";
        }
    }

    public static void demonstrate() throws InterruptedException, ExecutionException, TimeoutException {

        // ==================== BASIC FUTURE USAGE ====================

        System.out.println("\n>>> 1. BASIC FUTURE USAGE");
        System.out.println("Submit a Callable task and get a Future\n");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Submit a Callable task, get a Future
        Future<Integer> future1 = executor.submit(
            new NumberGeneratorTask("Task-1", 1, 10)
        );

        System.out.println("Task submitted. Doing other work...");
        Thread.sleep(500); // Do some other work
        System.out.println("Other work completed. Checking if task is done...");

        // Check if task is done without blocking
        if (future1.isDone()) {
            System.out.println("Task is already done!");
        } else {
            System.out.println("Task is still running...");
        }

        // Get the result (blocking - waits if not ready)
        System.out.println("Getting result (may block if not ready)...");
        Integer result1 = future1.get();
        System.out.println("Got result: " + result1);

        // ==================== MULTIPLE FUTURES ====================

        System.out.println("\n\n>>> 2. MULTIPLE FUTURES");
        System.out.println("Submit multiple tasks and collect results\n");

        Future<Integer> future2 = executor.submit(
            new NumberGeneratorTask("Task-2", 11, 20)
        );

        Future<String> future3 = executor.submit(
            new FileProcessorTask("data.txt")
        );

        System.out.println("Multiple tasks submitted. Collecting results...");

        int result2 = future2.get();
        String result3 = future3.get();

        System.out.println("Result 2: " + result2);
        System.out.println("Result 3: " + result3);

        // ==================== FUTURE WITH TIMEOUT ====================

        System.out.println("\n\n>>> 3. FUTURE WITH TIMEOUT");
        System.out.println("Get result with time limit\n");

        Future<Integer> future4 = executor.submit(
            new NumberGeneratorTask("Task-4", 21, 30)
        );

        try {
            System.out.println("Trying to get result with 1 second timeout...");
            Integer result4 = future4.get(1, TimeUnit.SECONDS);
            System.out.println("Got result: " + result4);
        } catch (TimeoutException e) {
            System.out.println("TIMEOUT! Task didn't complete within 1 second");
            System.out.println("Retrying with longer timeout...");
            Integer result4 = future4.get(5, TimeUnit.SECONDS);
            System.out.println("Got result on retry: " + result4);
        }

        // ==================== CANCELING A TASK ====================

        System.out.println("\n\n>>> 4. CANCELING A TASK");
        System.out.println("Cancel a running task\n");

        Future<Integer> future5 = executor.submit(
            new NumberGeneratorTask("Task-5", 31, 100)
        );

        Thread.sleep(500);
        System.out.println("Attempting to cancel Task-5...");

        // cancel(true) interrupts the task if running
        // cancel(false) doesn't interrupt, only prevents starting
        boolean cancelled = future5.cancel(true);

        if (cancelled) {
            System.out.println("Task was successfully cancelled");
        } else {
            System.out.println("Task could not be cancelled (already done)");
        }

        // ==================== EXCEPTION HANDLING ====================

        System.out.println("\n\n>>> 5. EXCEPTION HANDLING");
        System.out.println("Callable can throw exceptions\n");

        Future<String> future6 = executor.submit(
            new FailingTask(true)
        );

        try {
            System.out.println("Getting result from task that will fail...");
            String result6 = future6.get();
        } catch (ExecutionException e) {
            System.out.println("ExecutionException caught!");
            System.out.println("Cause: " + e.getCause().getMessage());
            System.out.println("This allows us to handle task failures");
        }

        // ==================== USING LAMBDA WITH CALLABLE ====================

        System.out.println("\n\n>>> 6. USING LAMBDA EXPRESSIONS");
        System.out.println("Modern way with lambda expressions\n");

        // Callable as lambda
        Future<Integer> future7 = executor.submit(() -> {
            System.out.println("Lambda callable started");
            Thread.sleep(1000);
            System.out.println("Lambda callable computing...");
            int result = 50 + 25;
            System.out.println("Lambda callable completed");
            return result;
        });

        System.out.println("Lambda task submitted");
        int result7 = future7.get();
        System.out.println("Lambda result: " + result7);

        // ==================== INVOKEANY ====================

        System.out.println("\n\n>>> 7. INVOKEANY - GET RESULT FROM FIRST COMPLETED TASK");
        System.out.println("Submit multiple tasks, return result from first completed\n");

        java.util.List<Callable<Integer>> tasks = new java.util.ArrayList<>();
        tasks.add(() -> { Thread.sleep(3000); return 100; }); // Slow
        tasks.add(() -> { Thread.sleep(500); return 200; });  // Fast
        tasks.add(() -> { Thread.sleep(2000); return 300; }); // Medium

        System.out.println("Submitting 3 tasks with different execution times...");
        long startTime = System.currentTimeMillis();

        Integer firstResult = executor.invokeAny(tasks);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Got result: " + firstResult + " (elapsed: " + elapsed + "ms)");
        System.out.println("(Result is from the fastest task)");

        // Shutdown
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n>>> ALL DEMONSTRATIONS COMPLETED");
    }
}

