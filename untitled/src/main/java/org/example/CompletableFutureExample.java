package org.example;

import java.util.concurrent.*;

/**
 * EXAMPLE 5: COMPLETABLE FUTURE (Java 8+)
 *
 * CompletableFuture is a modern, powerful abstraction for asynchronous programming.
 * It allows composing asynchronous computations in a functional style.
 *
 * HIGH-LEVEL CONCEPTS:
 * - Manually completable: You can complete it from anywhere, not just from a thread
 * - Functional composition: Chain operations with thenApply, thenAccept, etc.
 * - Combining futures: Combine multiple asynchronous operations
 * - Exception handling: Handle failures with exceptionally, handle, etc.
 *
 * WHY CompletableFuture IS POWERFUL:
 * - Replaces callback hell with clean functional chains
 * - Better error handling than callbacks
 * - Can be manually completed from any thread
 * - Supports both blocking (get) and reactive (thenXxx) styles
 */

public class CompletableFutureExample {

    // ==================== SIMULATED ASYNC OPERATIONS ====================

    /**
     * Simulates an async operation that takes time
     */
    static int fetchUserData(int userId) throws InterruptedException {
        System.out.println("  Fetching user " + userId + "...");
        Thread.sleep(1000);
        return userId * 100; // User salary
    }

    static String fetchUserName(int userId) throws InterruptedException {
        System.out.println("  Fetching name for user " + userId + "...");
        Thread.sleep(800);
        return "User-" + userId;
    }

    static double calculateBonus(int salary) throws InterruptedException {
        System.out.println("  Calculating bonus for salary " + salary + "...");
        Thread.sleep(500);
        return salary * 0.1; // 10% bonus
    }

    public static void demonstrate() throws InterruptedException, ExecutionException {

        // ==================== 1. CREATING COMPLETABLE FUTURES ====================

        System.out.println("\n>>> 1. CREATING COMPLETABLE FUTURES");
        System.out.println("Different ways to create CompletableFuture\n");

        // Approach 1: Already completed future
        CompletableFuture<String> completed = CompletableFuture.completedFuture("Hello");
        System.out.println("Already completed: " + completed.get());

        // Approach 2: Supply value asynchronously
        CompletableFuture<Integer> supply = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("  Supplying value asynchronously...");
                Thread.sleep(500);
                return 42;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Supplied value: " + supply.get());

        // Approach 3: Run asynchronously (void)
        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("  Running async task...");
                Thread.sleep(500);
                System.out.println("  Async task completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        runAsync.get();

        // ==================== 2. THENMAPPING - TRANSFORMING VALUES ====================

        System.out.println("\n\n>>> 2. THENMAPPING - TRANSFORMING VALUES");
        System.out.println("Apply transformations to results\n");

        CompletableFuture<Integer> futureNumber = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Producing number...");
            return 5;
        });

        // thenApply transforms the result
        CompletableFuture<Integer> doubled = futureNumber.thenApply(n -> {
            System.out.println("  Doubling " + n + "...");
            return n * 2;
        });

        // Chain multiple transformations
        CompletableFuture<String> stringResult = doubled.thenApply(n -> {
            System.out.println("  Converting " + n + " to string...");
            return "Result: " + n;
        });

        System.out.println("Final result: " + stringResult.get());

        // ==================== 3. THENACCEPT - CONSUMING VALUES ====================

        System.out.println("\n\n>>> 3. THENACCEPT - CONSUMING VALUES");
        System.out.println("Process results without returning values\n");

        CompletableFuture<Integer> futureValue = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Computing value...");
            return 100;
        });

        // thenAccept consumes the result
        CompletableFuture<Void> acceptance = futureValue.thenAccept(value -> {
            System.out.println("  Received value: " + value);
            System.out.println("  Processing it (no return value)");
        });

        acceptance.get(); // Wait for completion

        // ==================== 4. EXCEPTION HANDLING ====================

        System.out.println("\n\n>>> 4. EXCEPTION HANDLING");
        System.out.println("Handle failures gracefully\n");

        CompletableFuture<Integer> failingFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Starting computation...");
            throw new RuntimeException("Something went wrong!");
        });

        // exceptionally handles exceptions
        CompletableFuture<Integer> recovered = failingFuture.exceptionally(ex -> {
            System.out.println("  Exception caught: " + ex.getMessage());
            System.out.println("  Recovering with default value...");
            return -1;
        });

        System.out.println("Result after recovery: " + recovered.get());

        // handle method combines both success and failure
        CompletableFuture<String> handled = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Starting another computation...");
            throw new RuntimeException("Another error!");
        }).handle((result, exception) -> {
            if (exception != null) {
                System.out.println("  Error in handle: " + exception.getMessage());
                return "Handled error";
            } else {
                return "Success: " + result;
            }
        });

        System.out.println("Result: " + handled.get());

        // ==================== 5. COMBINING MULTIPLE FUTURES ====================

        System.out.println("\n\n>>> 5. COMBINING MULTIPLE FUTURES");
        System.out.println("Execute multiple async operations and combine results\n");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Two independent async operations
        CompletableFuture<String> userName = CompletableFuture.supplyAsync(
            () -> {
                try {
                    return fetchUserName(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            },
            executor
        );

        CompletableFuture<Integer> userSalary = CompletableFuture.supplyAsync(
            () -> {
                try {
                    return fetchUserData(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            },
            executor
        );

        // Combine results using thenCombine
        CompletableFuture<String> combined = userName.thenCombine(userSalary,
            (name, salary) -> {
                System.out.println("  Combining results: name=" + name + ", salary=" + salary);
                return name + " earns $" + salary;
            }
        );

        System.out.println("Combined result: " + combined.get());

        // ==================== 6. SEQUENTIAL CHAINING ====================

        System.out.println("\n\n>>> 6. SEQUENTIAL CHAINING");
        System.out.println("Chain async operations where output of one is input to next\n");

        // Get salary, then calculate bonus based on salary
        CompletableFuture<Double> bonusFuture = CompletableFuture
            .supplyAsync(() -> {
                try {
                    return fetchUserData(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor)
            .thenCompose(salary -> {
                // thenCompose flattens nested futures
                System.out.println("  Got salary, now calculating bonus...");
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        return calculateBonus(salary);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
            });

        System.out.println("Bonus: $" + bonusFuture.get());

        // ==================== 7. ALLOF - WAIT FOR ALL FUTURES ====================

        System.out.println("\n\n>>> 7. ALLOF - WAIT FOR ALL FUTURES");
        System.out.println("Execute multiple async operations and wait for all\n");

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("  Task 1 started");
                Thread.sleep(1000);
                System.out.println("  Task 1 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("  Task 2 started");
                Thread.sleep(800);
                System.out.println("  Task 2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executor);

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("  Task 3 started");
                Thread.sleep(600);
                System.out.println("  Task 3 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executor);

        // Wait for all to complete
        CompletableFuture.allOf(future1, future2, future3).get();
        System.out.println("All tasks completed!");

        // ==================== 8. ANYOF - FIRST TO COMPLETE ====================

        System.out.println("\n\n>>> 8. ANYOF - FIRST TO COMPLETE");
        System.out.println("Race multiple operations, use result from first\n");

        CompletableFuture<String> race1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                return "Slow result";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);

        CompletableFuture<String> race2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
                return "Fast result";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);

        CompletableFuture<String> race3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Medium result";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);

        // anyOf returns first completed future
        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(race1, race2, race3);
        System.out.println("First to complete: " + firstCompleted.get());

        // ==================== 9. MANUAL COMPLETION ====================

        System.out.println("\n\n>>> 9. MANUAL COMPLETION");
        System.out.println("Complete a future manually from any thread\n");

        CompletableFuture<String> manualFuture = new CompletableFuture<>();

        // Start a thread that will manually complete the future
        Thread completerThread = new Thread(() -> {
            try {
                System.out.println("  Completer thread working...");
                Thread.sleep(1000);
                System.out.println("  Completer thread completing the future");
                manualFuture.complete("Manually completed!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        completerThread.start();

        System.out.println("Waiting for manual completion...");
        System.out.println("Result: " + manualFuture.get());

        // ==================== 10. REAL-WORLD EXAMPLE ====================

        System.out.println("\n\n>>> 10. REAL-WORLD EXAMPLE");
        System.out.println("Fetching user data and calculating salary with benefits\n");

        int userId = 5;

        CompletableFuture<String> userInfo = CompletableFuture
            // Get user name
            .supplyAsync(() -> {
                try {
                    return fetchUserName(userId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor)
            // Get salary and combine
            .thenCombine(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetchUserData(userId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor),
                (name, salary) -> name + " -> Salary: $" + salary
            )
            // Calculate bonus
            .thenCompose(info ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // Extract salary from info string
                        int salary = Integer.parseInt(info.split("\\$")[1]);
                        double bonus = salary * 0.1;
                        return info + ", Bonus: $" + bonus;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
            )
            .exceptionally(ex -> "Error: " + ex.getMessage());

        System.out.println("Final info: " + userInfo.get());

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n>>> ALL COMPLETABLEFUTURE DEMONSTRATIONS COMPLETED");
    }
}

