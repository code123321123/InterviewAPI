package org.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * MULTITHREADING IN JAVA 17 - COMPREHENSIVE GUIDE
 *
 * This is the main entry point demonstrating different multithreading approaches.
 * Run different examples to understand various concurrency patterns.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("========== MULTITHREADING DEMONSTRATIONS IN JAVA 17 ==========\n");

        // Example 1: Basic Thread Creation and Management
        System.out.println("--- EXAMPLE 1: Basic Thread Creation ---");
        BasicThreadExample.demonstrate();

        System.out.println("\n--- EXAMPLE 2: Thread Synchronization & Mutual Exclusion ---");
        ThreadSynchronizationExample.demonstrate();

        System.out.println("\n--- EXAMPLE 3: ExecutorService & Thread Pool ---");
        ExecutorServiceExample.demonstrate();

        System.out.println("\n--- EXAMPLE 4: Callable & Future ---");
        CallableAndFutureExample.demonstrate();

        System.out.println("\n--- EXAMPLE 5: CompletableFuture (Java 8+) ---");
        CompletableFutureExample.demonstrate();

        System.out.println("\n--- EXAMPLE 6: Virtual Threads (Java 19+) ---");
        VirtualThreadsExample.demonstrate();

        System.out.println("\n========== END OF DEMONSTRATIONS ==========");
    }
}