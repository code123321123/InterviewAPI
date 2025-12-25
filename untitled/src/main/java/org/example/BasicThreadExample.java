package org.example;

/**
 * EXAMPLE 1: BASIC THREAD CREATION AND MANAGEMENT
 *
 * Understanding the fundamentals of creating and managing threads in Java.
 *
 * HIGH-LEVEL CONCEPTS:
 * - A Thread is a unit of execution within a process
 * - Java has two ways to create threads: extend Thread class or implement Runnable
 * - The start() method begins thread execution (never call run() directly)
 * - Each thread has its own call stack but shares heap memory
 */

public class BasicThreadExample {

    // ==================== APPROACH 1: EXTEND THREAD CLASS ====================

    /**
     * Traditional way - extend the Thread class
     * Simple but limits inheritance (Java doesn't support multiple inheritance)
     */
    static class MyThread extends Thread {
        private String threadName;
        private int iterations;

        public MyThread(String name, int iterations) {
            this.threadName = name;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            // This method is called when thread starts
            System.out.println(threadName + " started [ID: " + Thread.currentThread().getId() + "]");

            for (int i = 1; i <= iterations; i++) {
                System.out.println("  " + threadName + " - Iteration " + i + " (Priority: " +
                    Thread.currentThread().getPriority() + ")");

                try {
                    // Simulate some work with sleep
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println(threadName + " was interrupted!");
                    Thread.currentThread().interrupt(); // Restore interrupt status
                }
            }
            System.out.println(threadName + " finished");
        }
    }

    // ==================== APPROACH 2: IMPLEMENT RUNNABLE ====================

    /**
     * Modern way - implement Runnable interface
     * Preferred because class can extend another class and implement Runnable
     * More flexible for composition
     */
    static class RunnableTask implements Runnable {
        private String taskName;
        private int iterations;

        public RunnableTask(String name, int iterations) {
            this.taskName = name;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            System.out.println(taskName + " started [ID: " + Thread.currentThread().getId() + "]");

            for (int i = 1; i <= iterations; i++) {
                System.out.println("  " + taskName + " - Step " + i);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    System.out.println(taskName + " was interrupted!");
                    break;
                }
            }
            System.out.println(taskName + " completed");
        }
    }

    // ==================== APPROACH 3: LAMBDA EXPRESSION (Java 8+) ====================

    /**
     * Modern approach using lambda expressions
     * Most concise and readable for simple tasks
     */
    static void demonstrateLambda() {
        Thread thread = new Thread(() -> {
            System.out.println("Lambda thread started [ID: " + Thread.currentThread().getId() + "]");
            for (int i = 1; i <= 3; i++) {
                System.out.println("  Lambda - Task " + i);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Lambda thread completed");
        }, "LambdaWorker");

        thread.start();
    }

    public static void demonstrate() throws InterruptedException {
        System.out.println("Main thread ID: " + Thread.currentThread().getId());
        System.out.println("Main thread name: " + Thread.currentThread().getName());

        // Create threads using Thread extension
        System.out.println("\n>>> APPROACH 1: Extending Thread Class");
        MyThread thread1 = new MyThread("Thread-A", 3);
        MyThread thread2 = new MyThread("Thread-B", 3);

        // Important: call start(), not run()!
        // start() creates a new thread and calls run()
        // run() would execute in the same thread (WRONG!)
        thread1.start();
        thread2.start();

        // Create threads using Runnable
        System.out.println("\n>>> APPROACH 2: Implementing Runnable");
        Thread thread3 = new Thread(new RunnableTask("RunnableTask-1", 3));
        Thread thread4 = new Thread(new RunnableTask("RunnableTask-2", 3));
        thread3.start();
        thread4.start();

        // Lambda expression approach
        System.out.println("\n>>> APPROACH 3: Using Lambda Expression");
        demonstrateLambda();

        // ==================== THREAD MANAGEMENT ====================

        System.out.println("\n>>> WAITING FOR ALL THREADS TO COMPLETE");

        // join() waits for a thread to complete
        thread1.join(); // Main thread waits for thread1 to finish
        thread2.join(); // Main thread waits for thread2 to finish
        thread3.join();
        thread4.join();

        // Note: The lambda thread is daemon thread or main might exit before it completes
        // To ensure it finishes, we'd need to store the reference and join
        Thread.sleep(2000); // Give lambda thread time to complete

        System.out.println("\n>>> ALL THREADS HAVE COMPLETED");
        System.out.println("Main thread is finishing...");
    }
}

