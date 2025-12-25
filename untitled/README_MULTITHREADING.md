================================================================================
JAVA 17 MULTITHREADING - COMPREHENSIVE GUIDE
================================================================================

This project demonstrates 6 different approaches to multithreading in Java,
with detailed comments and examples for each concept.

================================================================================
TABLE OF CONTENTS
================================================================================

1. BasicThreadExample.java
   - Thread creation by extending Thread class
   - Thread creation by implementing Runnable
   - Lambda expression approach
   - Thread management with join()

2. ThreadSynchronizationExample.java
   - Race condition problems
   - Synchronized methods
   - Synchronized blocks
   - ReentrantLock
   - Producer-Consumer pattern with wait/notify

3. ExecutorServiceExample.java
   - Fixed Thread Pool
   - Cached Thread Pool
   - Single Thread Executor
   - Scheduled Thread Pool
   - Task submission and result retrieval
   - invokeAll() for batch operations

4. CallableAndFutureExample.java
   - Callable interface (like Runnable but with return value)
   - Future<T> for retrieving results
   - Future with timeout
   - Canceling tasks
   - Exception handling
   - invokeAny() for competitive execution

5. CompletableFutureExample.java
   - Modern async programming in Java
   - Functional composition (thenApply, thenAccept)
   - Chaining operations
   - Exception handling with exceptionally/handle
   - Combining multiple futures (thenCombine, thenCompose)
   - allOf() - wait for all operations
   - anyOf() - first completed wins
   - Manual completion
   - Real-world example

6. VirtualThreadsExample.java
   - Virtual Threads (Java 19+)
   - Creating massive numbers of lightweight threads
   - Virtual thread executor
   - Structured concurrency
   - Perfect for I/O-bound workloads
   - Java 17 compatible alternatives

================================================================================
QUICK REFERENCE: WHEN TO USE WHAT
================================================================================

USE Thread/Runnable WHEN:
✓ Simple one-off thread creation
✓ Full control over thread lifecycle
✓ Learning threading basics
✗ Not recommended for modern code

USE ExecutorService WHEN:
✓ Managing multiple concurrent tasks
✓ Need to control thread count
✓ Using thread pools for efficiency
✓ Want to submit tasks from multiple sources

USE Callable + Future WHEN:
✓ Need return values from async operations
✓ Need to wait for specific results with timeout
✓ Want to cancel operations
✓ Need checked exception handling

USE CompletableFuture WHEN:
✓ Building complex async pipelines
✓ Combining multiple async operations
✓ Want clean functional/reactive style
✓ Need better composability than Future
✓ Modern Java application architecture

USE Virtual Threads (Java 19+) WHEN:
✓ Need massive concurrency (thousands+ of threads)
✓ I/O-bound operations (network, database)
✓ Can't use callbacks or complex async patterns
✓ Want simple blocking code that's efficient

================================================================================
HIGH-LEVEL CONCEPTS EXPLAINED
================================================================================

1. THREADS
   A thread is a unit of execution within a process.
   - Each thread has its own call stack
   - All threads share heap memory
   - Running on multi-core systems allows true parallelism
   - On single core, OS switches between threads (time-sharing)

2. RACE CONDITION
   Multiple threads accessing shared data without synchronization.
   Example: Two threads both incrementing count++
   - Thread A: reads count (5) → computes 6 → writes 6
   - Thread B: reads count (5) → computes 6 → writes 6
   - Result: 6 instead of 7!

3. SYNCHRONIZATION / LOCKS
   Mechanism to ensure only one thread executes critical section.
   Types:
   - Synchronized (built-in Java monitor)
   - ReentrantLock (more control, can be fair)
   - ReadWriteLock (multiple readers, single writer)

4. THREAD POOL / EXECUTOR
   Pre-created threads waiting for tasks.
   Benefits:
   - Reduces thread creation overhead
   - Limits thread count (prevents resource exhaustion)
   - Better CPU cache utilization
   - Simplified thread management

5. COMPLETABLE FUTURE
   Modern way to handle async operations.
   Instead of: callback -> callback -> callback (callback hell)
   Use:        future.thenApply().thenCompose().handle()
   
   Advantages:
   - Readable, functional style
   - Easy error handling
   - Easy to combine multiple operations
   - Don't need complex async/await

6. VIRTUAL THREADS
   Lightweight threads managed by JVM (not OS).
   - Millions possible vs thousands of platform threads
   - Ideal for I/O-bound workloads
   - Can write blocking code that's actually async
   - Automatically manages context switching

================================================================================
EXECUTION FLOW EXAMPLES
================================================================================

EXAMPLE 1: Simple Thread Creation
Main Thread
├─ Thread A: 1 → 2 → 3 ✓
├─ Thread B: 1 → 2 → 3 ✓
└─ (May interleave: A1, B1, A2, B2, A3, B3)

EXAMPLE 2: synchronized increment with 5 threads
Thread 1: lock → count++ → unlock
Thread 2: [waits] → lock → count++ → unlock
Thread 3: [waits] → lock → count++ → unlock
... final count is always 5000

EXAMPLE 3: Producer-Consumer
Producer: put(1) → notify() → put(2) → notify() → ...
Consumer: [wait] ← get() → [wait] ← get() → ...

EXAMPLE 4: CompletableFuture chain
supplyAsync(getUserData)
  → thenApply(calculateBonus)
  → thenCompose(fetchDetails)
  → thenAccept(print)
  → handle(errors)

EXAMPLE 5: Virtual Threads
Main Thread spawns:
├─ VirtualThread 1 (I/O operation)
├─ VirtualThread 2 (I/O operation)
├─ VirtualThread 3 (I/O operation)
... (10,000 more threads possible!)
└─ All managed efficiently by JVM

================================================================================
HOW TO RUN THE EXAMPLES
================================================================================

From command line:
  cd C:\Users\rakhi\IdeaProjects\untitled
  javac -d target/classes src/main/java/org/example/*.java
  java -cp target/classes org.example.Main

Or in IDE:
  - Right-click Main.java → Run
  - Or click the green Run arrow next to main()

The program will run all 6 examples with detailed output.

================================================================================
KEY TAKEAWAYS
================================================================================

1. Never manually create/manage threads for production code
   Use ExecutorService or Virtual Threads instead

2. Always protect shared data with synchronization
   Consequences of not doing this: Data corruption, deadlocks

3. For modern async code, prefer CompletableFuture over callbacks
   It's cleaner, easier to understand, and more maintainable

4. Use try-with-resources or proper shutdown for executors
   Otherwise thread pools may not terminate properly

5. Virtual Threads (Java 19+) will likely become the standard
   They make concurrent programming much simpler

6. I/O operations scale much better with Virtual Threads
   Blocking I/O becomes efficient, no callbacks needed

================================================================================
BEST PRACTICES
================================================================================

✓ DO:
  - Use ExecutorService for thread pool management
  - Use CompletableFuture for async composition
  - Use synchronized only for simple critical sections
  - Use ReentrantLock for complex locking scenarios
  - Always shutdown executors properly
  - Test concurrent code thoroughly
  - Use try-catch for thread interruption

✗ DON'T:
  - Don't manually create/manage threads
  - Don't hold locks during I/O operations
  - Don't ignore InterruptedException
  - Don't call run() directly (use start() instead)
  - Don't forget to join() or shutdown()
  - Don't mix different synchronization mechanisms
  - Don't rely on thread execution order

================================================================================
COMMON PATTERNS
================================================================================

Pattern 1: Thread Pool for Independent Tasks
ExecutorService executor = Executors.newFixedThreadPool(10);
for (Task t : tasks) {
    executor.submit(t);
}
executor.shutdown();
executor.awaitTermination(timeout, unit);

Pattern 2: Async Processing with CompletableFuture
CompletableFuture
    .supplyAsync(this::fetchData)
    .thenApply(this::processData)
    .thenAccept(this::saveData)
    .exceptionally(ex -> { log(ex); return null; });

Pattern 3: Collect Results from Multiple Async Operations
List<CompletableFuture<Result>> futures = ...;
CompletableFuture.allOf(futures.toArray(...))
    .thenApply(v -> futures.stream().map(f -> f.join()).collect(...))
    .thenAccept(this::process);

Pattern 4: Timeout for Long Operations
Future<Result> future = executor.submit(task);
try {
    Result result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
}

================================================================================
ERROR HANDLING
================================================================================

Race Condition → Race conditions lead to unpredictable behavior
  Solution: Use synchronized/locks to protect shared state

Deadlock → Multiple threads waiting on each other forever
  Solution: Always acquire locks in same order, use timeouts

Thread Starvation → Low-priority threads never get CPU time
  Solution: Avoid extreme priority differences

Exception in Thread → Can't bubble up to main() naturally
  Solution: Use Thread.setUncaughtExceptionHandler() or
            ExecutorService to properly handle exceptions

InterruptedException → Thread interrupted while sleeping/blocking
  Solution: Catch it, restore interrupt status, or propagate up
           if (!Thread.currentThread().isInterrupted()) {
               Thread.currentThread().interrupt();
           }

================================================================================

