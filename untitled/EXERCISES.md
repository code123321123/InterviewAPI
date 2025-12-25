================================================================================
JAVA MULTITHREADING - PRACTICE EXERCISES & CHALLENGES
================================================================================

This file contains exercises to practice multithreading concepts.
Solutions are included below each exercise.

================================================================================
EXERCISE 1: BASIC THREAD CREATION
================================================================================

TASK: Create 5 threads that print numbers 1-5, each thread prints its own number
multiple times.

STARTER CODE:
-------------
public class Exercise1 {
    public static void main(String[] args) throws InterruptedException {
        // TODO: Create 5 threads
        // Each thread i should print "Thread i: 1, 2, 3, 4, 5"
        // Wait for all threads to finish
    }
}

SOLUTION:
---------
public class Exercise1 {
    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        for (int i = 1; i <= 5; i++) {
            final int threadNum = i;
            threads[i-1] = new Thread(() -> {
                for (int j = 1; j <= 5; j++) {
                    System.out.println("Thread " + threadNum + ": " + j);
                }
            }, "Worker-" + i);
            threads[i-1].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("All threads completed!");
    }
}

KEY LEARNING:
- new Thread() with lambda expression
- start() vs run()
- join() to wait for completion
- Thread naming


================================================================================
EXERCISE 2: RACE CONDITION
================================================================================

TASK: Fix the race condition in the following code:

BROKEN CODE:
-----------
public class BankAccount {
    private int balance = 1000;
    
    public void deposit(int amount) {
        balance += amount;  // RACE CONDITION!
    }
    
    public void withdraw(int amount) {
        balance -= amount;  // RACE CONDITION!
    }
    
    public int getBalance() {
        return balance;
    }
    
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();
        
        // 10 threads, each deposits $100
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    account.deposit(1);
                }
            }).start();
        }
        
        Thread.sleep(2000);
        System.out.println("Expected: 2000, Actual: " + account.getBalance());
    }
}

SOLUTION (Using synchronized):
------------------------------
public class BankAccount {
    private int balance = 1000;
    
    public synchronized void deposit(int amount) {
        balance += amount;  // Protected!
    }
    
    public synchronized void withdraw(int amount) {
        balance -= amount;  // Protected!
    }
    
    public synchronized int getBalance() {
        return balance;
    }
}

SOLUTION (Using ReentrantLock):
-------------------------------
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private int balance = 1000;
    private final Lock lock = new ReentrantLock();
    
    public void deposit(int amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }
    
    public void withdraw(int amount) {
        lock.lock();
        try {
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }
    
    public int getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
}

KEY LEARNING:
- synchronized methods protect entire method
- synchronized blocks protect specific code
- ReentrantLock with try-finally pattern
- Lock is held across object, only one thread at a time


================================================================================
EXERCISE 3: PRODUCER-CONSUMER WITH BLOCKING QUEUE
================================================================================

TASK: Implement a producer-consumer using BlockingQueue.

STARTER CODE:
-----------
public class Exercise3 {
    public static void main(String[] args) throws InterruptedException {
        // TODO: Create BlockingQueue
        // TODO: Create producer thread that puts 10 items
        // TODO: Create consumer thread that takes 10 items
        // TODO: Print consumed items
    }
}

SOLUTION:
---------
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Exercise3 {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    System.out.println("Producing: " + i);
                    queue.put(i);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    int item = queue.take();
                    System.out.println("Consumed: " + item);
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("Done!");
    }
}

KEY LEARNING:
- BlockingQueue.put() blocks when full
- BlockingQueue.take() blocks when empty
- Automatic synchronization handled by queue
- Better than manual wait/notify


================================================================================
EXERCISE 4: EXECUTOR SERVICE
================================================================================

TASK: Submit 20 tasks to a fixed thread pool of 4 threads.
Count successful completions.

STARTER CODE:
-----------
import java.util.concurrent.*;

public class Exercise4 {
    public static void main(String[] args) throws Exception {
        // TODO: Create ExecutorService with 4 threads
        // TODO: Submit 20 tasks (simple work)
        // TODO: Count completions
        // TODO: Shutdown and await termination
    }
}

SOLUTION:
---------
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Exercise4 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger completed = new AtomicInteger(0);
        
        System.out.println("Submitting 20 tasks to 4-thread pool...");
        
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Task " + taskId + " started [Thread: " + 
                        Thread.currentThread().getName() + "]");
                    Thread.sleep(1000);
                    System.out.println("Task " + taskId + " completed");
                    completed.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        System.out.println("Total completed: " + completed.get());
    }
}

KEY LEARNING:
- ExecutorService manages thread pool
- Tasks queued when all threads busy
- shutdown() + awaitTermination() for clean shutdown
- AtomicInteger for thread-safe counter


================================================================================
EXERCISE 5: CALLABLE & FUTURE
================================================================================

TASK: Create tasks that calculate sum of numbers. Use Callable to return results.

STARTER CODE:
-----------
import java.util.concurrent.*;

public class Exercise5 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // TODO: Create 3 Callable tasks
        // Task 1: Sum 1-10
        // Task 2: Sum 11-20
        // Task 3: Sum 21-30
        
        // TODO: Get results and print
        
        executor.shutdown();
    }
}

SOLUTION:
---------
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class Exercise5 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<>();
        
        // Submit callable tasks
        for (int start = 1; start <= 21; start += 10) {
            final int s = start;
            final int e = start + 9;
            
            Future<Integer> future = executor.submit(() -> {
                int sum = 0;
                for (int i = s; i <= e; i++) {
                    sum += i;
                }
                System.out.println("Task sum(" + s + "-" + e + ") = " + sum);
                return sum;
            });
            
            futures.add(future);
        }
        
        // Collect results
        int total = 0;
        for (Future<Integer> f : futures) {
            int result = f.get();  // Blocks until ready
            total += result;
        }
        
        System.out.println("Total sum: " + total);
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}

KEY LEARNING:
- Callable returns value (unlike Runnable)
- Future.get() blocks until result ready
- Collect multiple futures and process results
- Exception handling with ExecutionException


================================================================================
EXERCISE 6: COMPLETABLE FUTURE PIPELINE
================================================================================

TASK: Build async pipeline: fetch data → transform → calculate → print

STARTER CODE:
-----------
import java.util.concurrent.*;

public class Exercise6 {
    public static void main(String[] args) throws Exception {
        // TODO: Create pipeline using CompletableFuture
        // 1. supplyAsync(fetchData)
        // 2. thenApply(transform)
        // 3. thenCompose(calculateAsync)
        // 4. thenAccept(printResult)
    }
}

SOLUTION:
---------
import java.util.concurrent.*;

public class Exercise6 {
    static int fetchData() throws InterruptedException {
        System.out.println("Fetching data...");
        Thread.sleep(1000);
        return 100;
    }
    
    static int transform(int data) {
        System.out.println("Transforming: " + data);
        return data * 2;
    }
    
    static CompletableFuture<Integer> calculateAsync(int value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Calculating...");
                Thread.sleep(500);
                return value + 50;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public static void main(String[] args) throws Exception {
        CompletableFuture<Integer> pipeline = 
            CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchData();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            })
            .thenApply(Exercise6::transform)
            .thenCompose(Exercise6::calculateAsync)
            .thenApply(result -> {
                System.out.println("Final result: " + result);
                return result;
            })
            .exceptionally(ex -> {
                System.err.println("Error: " + ex.getMessage());
                return -1;
            });
        
        Integer result = pipeline.get();
        System.out.println("Got: " + result);
    }
}

KEY LEARNING:
- supplyAsync starts async operation
- thenApply transforms synchronously
- thenCompose chains async operations
- thenAccept consumes result
- exceptionally handles errors


================================================================================
EXERCISE 7: COMBINING MULTIPLE COMPLETABLE FUTURES
================================================================================

TASK: Fetch user data and profile concurrently, combine results.

SOLUTION:
---------
import java.util.concurrent.*;

public class Exercise7 {
    static String fetchUserName() throws InterruptedException {
        System.out.println("Fetching username...");
        Thread.sleep(1000);
        return "John Doe";
    }
    
    static Integer fetchUserAge() throws InterruptedException {
        System.out.println("Fetching age...");
        Thread.sleep(800);
        return 30;
    }
    
    public static void main(String[] args) throws Exception {
        CompletableFuture<String> nameFuture = 
            CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchUserName();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        
        CompletableFuture<Integer> ageFuture = 
            CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchUserAge();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        
        // Combine both results
        CompletableFuture<String> combined = 
            nameFuture.thenCombine(ageFuture, (name, age) -> {
                return name + " is " + age + " years old";
            });
        
        System.out.println(combined.get());
    }
}

KEY LEARNING:
- Run multiple async operations in parallel
- Use thenCombine to merge results
- Both execute concurrently, not sequentially


================================================================================
EXERCISE 8: VIRTUAL THREADS
================================================================================

TASK: Create 100,000 virtual threads and count completions.

STARTER CODE (Java 19+):
------------------------
public class Exercise8 {
    public static void main(String[] args) throws Exception {
        // TODO: Create 100,000 virtual threads
        // Each thread sleeps for 100ms (simulating I/O)
        // Count how many complete
    }
}

SOLUTION (Java 19+):
--------------------
import java.util.concurrent.atomic.AtomicInteger;

public class Exercise8 {
    public static void main(String[] args) throws Exception {
        AtomicInteger completed = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        Thread[] virtualThreads = new Thread[100000];
        
        for (int i = 0; i < 100000; i++) {
            final int taskId = i;
            virtualThreads[i] = Thread.startVirtualThread(() -> {
                try {
                    // Simulate I/O operation
                    Thread.sleep(100);
                    completed.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Wait for all
        for (Thread t : virtualThreads) {
            t.join();
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Completed: " + completed.get());
        System.out.println("Time: " + elapsed + "ms");
    }
}

SOLUTION (Java 17 - Using ExecutorService):
--------------------------------------------
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Exercise8 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        AtomicInteger completed = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                    completed.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Completed: " + completed.get());
        System.out.println("Time: " + elapsed + "ms");
    }
}

KEY LEARNING:
- Virtual threads enable massive concurrency
- Use Thread.startVirtualThread() (Java 19+)
- Each thread is lightweight
- Platform threads would run out of resources


================================================================================
CHALLENGE 1: DEADLOCK DETECTION
================================================================================

TASK: The following code can deadlock. Find and fix it.

BROKEN CODE:
-----------
public class DeadlockExample {
    static class Account {
        int balance = 100;
        
        synchronized void transferTo(Account other, int amount) {
            if (balance >= amount) {
                balance -= amount;
                other.deposit(amount);  // DEADLOCK RISK!
            }
        }
        
        synchronized void deposit(int amount) {
            balance += amount;
        }
    }
    
    public static void main(String[] args) throws Exception {
        Account a = new Account();
        Account b = new Account();
        
        // Thread 1: A -> B
        Thread t1 = new Thread(() -> a.transferTo(b, 50));
        
        // Thread 2: B -> A (DEADLOCK!)
        Thread t2 = new Thread(() -> b.transferTo(a, 50));
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
    }
}

SOLUTION:
---------
// Lock objects in consistent order
public class Account {
    int balance = 100;
    
    void transferTo(Account other, int amount) {
        // Always lock this first, then other
        Account first, second;
        if (System.identityHashCode(this) < System.identityHashCode(other)) {
            first = this;
            second = other;
        } else {
            first = other;
            second = this;
        }
        
        synchronized (first) {
            synchronized (second) {
                if (balance >= amount) {
                    balance -= amount;
                    other.balance += amount;
                }
            }
        }
    }
}

KEY LEARNING:
- Nested locks must be acquired in consistent order
- A→B→C is OK
- A→C→B→A causes deadlock
- Design to avoid nested locks when possible


================================================================================
CHALLENGE 2: BUILD A THREAD-SAFE COUNTER WITH STATISTICS
================================================================================

TASK: Create a counter that tracks:
- Current count
- Total increments
- Max concurrent access

SOLUTION:
---------
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeCounter {
    private int count = 0;
    private long totalIncrements = 0;
    private int maxConcurrent = 0;
    private AtomicInteger currentConcurrent = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();
    
    public void increment() {
        int current = currentConcurrent.incrementAndGet();
        if (current > maxConcurrent) {
            maxConcurrent = current;
        }
        
        lock.lock();
        try {
            count++;
            totalIncrements++;
        } finally {
            lock.unlock();
            currentConcurrent.decrementAndGet();
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
    
    public void printStats() {
        lock.lock();
        try {
            System.out.println("Count: " + count);
            System.out.println("Total Increments: " + totalIncrements);
            System.out.println("Max Concurrent: " + maxConcurrent);
        } finally {
            lock.unlock();
        }
    }
}

================================================================================
NEXT STEPS
================================================================================

After completing these exercises:

1. Understand when to use each synchronization mechanism
2. Run each example multiple times (race conditions are probabilistic)
3. Use tools to detect deadlocks and race conditions
4. Read Java Memory Model documentation
5. Study performance implications of different approaches
6. Practice on real projects

================================================================================

