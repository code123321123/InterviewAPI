================================================================================
JAVA MULTITHREADING - QUICK REFERENCE CHEAT SHEET
================================================================================

================================================================================
1. BASIC THREAD OPERATIONS
================================================================================

CREATE AND START THREAD:
------------------------
// Method 1: Extend Thread
class MyThread extends Thread {
    public void run() { /* code */ }
}
new MyThread().start();

// Method 2: Implement Runnable (Preferred)
Thread t = new Thread(() -> { /* code */ });
t.start();

// IMPORTANT: Always call start(), never run()!
// run() executes in same thread, start() creates new thread


THREAD LIFECYCLE METHODS:
-------------------------
t.start()               - Begin thread execution
t.join()                - Wait for thread to complete (blocking)
t.sleep(1000)           - Sleep for 1000ms (static method)
t.interrupt()           - Interrupt the thread
t.isAlive()             - Check if thread is alive
t.getName()             - Get thread name
t.getPriority()         - Get priority (1-10)
t.setPriority(5)        - Set priority


================================================================================
2. SYNCHRONIZATION
================================================================================

SYNCHRONIZED METHOD:
--------------------
public synchronized void increment() {
    count++;  // Protected by object's lock
}

// Only one thread can call this at a time
// Lock is on 'this' object


SYNCHRONIZED BLOCK:
-------------------
public void increment() {
    synchronized (this) {
        count++;  // Protected by object's lock
    }
}

// More efficient - only protects critical section
// Can synchronize on different object


REENTRANT LOCK:
---------------
private final Lock lock = new ReentrantLock();

public void increment() {
    lock.lock();
    try {
        count++;
    } finally {
        lock.unlock();  // Always unlock in finally!
    }
}

// More powerful than synchronized
// Can use tryLock(), fair queuing, conditions


READ-WRITE LOCK:
----------------
private final ReadWriteLock lock = new ReentrantReadWriteLock();

public void read() {
    lock.readLock().lock();
    try {
        // Multiple readers allowed
    } finally {
        lock.readLock().unlock();
    }
}

public void write() {
    lock.writeLock().lock();
    try {
        // Exclusive write access
    } finally {
        lock.writeLock().unlock();
    }
}


WAIT/NOTIFY (Condition Variables):
-----------------------------------
// Producer
synchronized void put(int value) {
    while (buffer.isFull()) {
        wait();  // Release lock, wait for notify
    }
    buffer.add(value);
    notifyAll();  // Wake up waiters
}

// Consumer
synchronized int get() {
    while (buffer.isEmpty()) {
        wait();
    }
    int value = buffer.remove();
    notifyAll();
    return value;
}


================================================================================
3. EXECUTOR SERVICE & THREAD POOLS
================================================================================

CREATE EXECUTOR:
----------------
// Fixed size pool
ExecutorService exec = Executors.newFixedThreadPool(10);

// Cached pool (grows as needed)
ExecutorService exec = Executors.newCachedThreadPool();

// Single thread executor
ExecutorService exec = Executors.newSingleThreadExecutor();

// Scheduled executor
ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);

// Virtual thread executor (Java 21+)
ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();


SUBMIT TASKS:
-------------
// Runnable (no return value)
executor.execute(() -> System.out.println("Task"));

// Callable (with return value)
Future<Integer> future = executor.submit(() -> {
    return 42;
});

// Multiple tasks
List<Future<Integer>> futures = new ArrayList<>();
for (Task t : tasks) {
    futures.add(executor.submit(t::execute));
}


SHUTDOWN:
---------
executor.shutdown();  // No new tasks accepted
// Wait for completion
executor.awaitTermination(10, TimeUnit.SECONDS);

// OR force shutdown
executor.shutdownNow();  // Cancel running tasks


GET RESULTS:
-----------
Future<Integer> f = executor.submit(() -> 42);

Integer result = f.get();  // Blocks until ready
Integer result = f.get(1, TimeUnit.SECONDS);  // With timeout

if (f.isDone()) {  // Check without blocking
    result = f.get();
}

if (f.cancel(true)) {  // Cancel task
    System.out.println("Task cancelled");
}


BATCH OPERATIONS:
-----------------
// Wait for all to complete
List<Future<T>> futures = executor.invokeAll(tasks);

// Get result from first completed
T result = executor.invokeAny(tasks);


================================================================================
4. CALLABLE & FUTURE
================================================================================

CREATE CALLABLE:
----------------
Callable<Integer> task = () -> {
    // Can throw checked exceptions
    // Must return a value
    return 42;
};

Future<Integer> future = executor.submit(task);


GET RESULT:
-----------
try {
    Integer result = future.get();  // Blocking
    Integer result = future.get(1, TimeUnit.SECONDS);  // Timeout
    
    if (!future.isDone()) {
        System.out.println("Still running");
    }
    
    if (future.cancel(true)) {
        System.out.println("Cancelled");
    }
    
} catch (TimeoutException e) {
    future.cancel(true);
} catch (ExecutionException e) {
    // Task threw exception
    Throwable cause = e.getCause();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}


================================================================================
5. COMPLETABLE FUTURE (Modern Async)
================================================================================

CREATE:
-------
// Completed value
CompletableFuture<String> cf = CompletableFuture.completedFuture("hello");

// Async supply
CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 42);

// Async without return
CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
    System.out.println("Running");
});

// Manual completion
CompletableFuture<String> cf = new CompletableFuture<>();
cf.complete("value");  // Later
cf.completeExceptionally(new Exception());  // Or error


CHAIN OPERATIONS:
-----------------
CompletableFuture
    .supplyAsync(() -> getData())
    .thenApply(data -> transform(data))  // Transform result
    .thenCompose(result -> asyncOp(result))  // Chain async
    .thenAccept(result -> consume(result))  // Final step
    .exceptionally(ex -> {  // Error handling
        log(ex);
        return defaultValue;
    });


COMBINE FUTURES:
----------------
CompletableFuture<String> cf1 = ...;
CompletableFuture<Integer> cf2 = ...;

// Combine both results
CompletableFuture<String> cf3 = cf1.thenCombine(cf2, 
    (str, num) -> str + num
);

// Or use both but only return one
CompletableFuture<String> cf3 = cf1.thenCombineAsync(cf2,
    (str, num) -> process(str, num),
    executor
);


WAIT FOR MULTIPLE:
------------------
// All must complete
CompletableFuture.allOf(cf1, cf2, cf3)
    .thenApply(v -> {
        // All done
        return cf1.join() + cf2.join() + cf3.join();
    });

// Any can complete
CompletableFuture.anyOf(cf1, cf2, cf3)
    .thenApply(result -> (String)result);


ERROR HANDLING:
---------------
cf.exceptionally(ex -> defaultValue)  // Return default
cf.handle((result, ex) -> {  // Handle both
    return ex != null ? defaultValue : result;
});
cf.whenComplete((result, ex) -> {  // Execute after
    if (ex != null) log(ex);
});


================================================================================
6. VIRTUAL THREADS (Java 19+)
================================================================================

CREATE VIRTUAL THREAD:
----------------------
// Single thread
Thread vt = Thread.ofVirtual()
    .name("virtual-1")
    .start(() -> {
        // Code
    });

// Or shorter
Thread vt = Thread.startVirtualThread(() -> {
    // Code
});


VIRTUAL THREAD EXECUTOR:
------------------------
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Use like normal executor
executor.submit(() -> System.out.println("Running on virtual thread"));


WAIT FOR COMPLETION:
--------------------
vt.join();  // Same as platform threads

// Multiple virtual threads
Thread[] threads = new Thread[10000];
for (int i = 0; i < 10000; i++) {
    threads[i] = Thread.startVirtualThread(() -> doWork());
}
for (Thread t : threads) {
    t.join();
}


KEY DIFFERENCES FROM PLATFORM THREADS:
---------------------------------------
- Can create millions (vs thousands)
- No need for thread pools
- Lightweight (low memory overhead)
- Perfect for I/O operations
- Same API as platform threads


================================================================================
7. COMMON PATTERNS
================================================================================

PATTERN: Fire and Forget
------------------------
executor.execute(() -> backgroundTask());


PATTERN: Wait for Result
------------------------
Future<T> future = executor.submit(callable);
T result = future.get();


PATTERN: Timeout
----------------
try {
    T result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
}


PATTERN: Process Multiple Results
----------------------------------
List<Future<T>> futures = new ArrayList<>();
for (Task t : tasks) {
    futures.add(executor.submit(t));
}
for (Future<T> f : futures) {
    T result = f.get();
}


PATTERN: Async Pipeline
-----------------------
CompletableFuture
    .supplyAsync(fetchData)
    .thenApply(processData)
    .thenCompose(enrichData)
    .thenAccept(saveData)
    .exceptionally(handleError);


PATTERN: Parallel Processing
-----------------------------
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture[] tasks = new CompletableFuture[100];

for (int i = 0; i < 100; i++) {
    final int id = i;
    tasks[i] = CompletableFuture.supplyAsync(
        () -> process(id),
        executor
    );
}

CompletableFuture.allOf(tasks).get();
executor.shutdown();


PATTERN: Circuit Breaker (Simple)
----------------------------------
AtomicInteger failures = new AtomicInteger();
try {
    result = future.get(5, TimeUnit.SECONDS);
    failures.set(0);
} catch (TimeoutException e) {
    failures.incrementAndGet();
    if (failures.get() > 3) {
        // Circuit open - stop trying
    }
}


================================================================================
8. DEBUGGING MULTITHREADED CODE
================================================================================

COMMON ISSUES:
--------------
1. Race Condition
   Symptom: Inconsistent results, unpredictable behavior
   Fix: Synchronize access to shared state

2. Deadlock
   Symptom: Program hangs, threads waiting forever
   Fix: Always acquire locks in same order, use timeouts

3. Thread Starvation
   Symptom: Some threads never get CPU time
   Fix: Check thread priorities, avoid long synchronization

4. Memory Visibility
   Symptom: Thread sees stale data
   Fix: Use synchronized or volatile

5. Livelock
   Symptom: Threads busy but no progress
   Fix: Redesign synchronization logic


DEBUGGING TECHNIQUES:
---------------------
// Add logging
System.out.println("Thread " + Thread.currentThread().getName());

// Monitor thread count
ThreadGroup tg = Thread.currentThread().getThreadGroup();
System.out.println("Active threads: " + tg.activeCount());

// Use thread names
Thread t = new Thread(() -> {}, "DescriptiveName");

// Check for deadlocks
ThreadMXBean bean = ManagementFactory.getThreadMXBean();
long[] deadlockedThreads = bean.findDeadlockedThreads();

// Set exception handler
Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
    System.err.println("Exception in " + t.getName());
    e.printStackTrace();
});


================================================================================
9. VOLATILE & ATOMIC
================================================================================

VOLATILE:
---------
// Ensures visibility across threads
private volatile boolean flag;

// Must use for:
// - Flags checked by multiple threads
// - Simple boolean states
// - NOT complex operations!

if (flag) {  // Always sees latest value
    doSomething();
}


ATOMIC:
-------
import java.util.concurrent.atomic.*;

AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // Thread-safe increment
count.compareAndSet(0, 1);  // CAS operation

// Better than synchronized for simple operations
// Lock-free (uses hardware support)


ATOMIC OPERATIONS:
------------------
AtomicInteger, AtomicLong, AtomicBoolean
AtomicReference<T>
AtomicIntegerArray, AtomicLongArray
AtomicReferenceArray<T>


================================================================================
10. THREAD SAFETY CHECKLIST
================================================================================

□ Identify shared mutable state
□ Protect with synchronization (lock, synchronized, atomic)
□ Use consistent locking (always lock in same order)
□ Minimize lock scope
□ Use appropriate lock type (synchronized, ReentrantLock, atomic)
□ Handle InterruptedException properly
□ Test with concurrent load
□ Use thread-safe collections (ConcurrentHashMap, etc.)
□ Avoid nested locks when possible
□ Document thread safety assumptions
□ Use tools: FindBugs, Thread Sanitizer, stress tests


================================================================================
11. THREAD-SAFE COLLECTIONS
================================================================================

USE THESE (Thread-Safe):
------------------------
ConcurrentHashMap       - Concurrent Map
CopyOnWriteArrayList    - Concurrent List (copy-on-write)
ConcurrentLinkedQueue   - Unbounded queue
BlockingQueue           - Queue with blocking operations
PriorityBlockingQueue   - Priority queue (blocking)
SynchronousQueue        - Handoff queue
DelayQueue              - Delayed elements
ConcurrentSkipListMap   - Concurrent sorted map


NOT THESE (NOT Thread-Safe):
----------------------------
HashMap                 - Use ConcurrentHashMap
ArrayList               - Use CopyOnWriteArrayList
LinkedList              - Use ConcurrentLinkedQueue
TreeMap                 - Use ConcurrentSkipListMap


SYNCHRONIZED WRAPPERS:
----------------------
Collections.synchronizedMap(new HashMap<>())
Collections.synchronizedList(new ArrayList<>())


================================================================================
12. MEMORY MODEL
================================================================================

HAPPENS-BEFORE RELATIONSHIPS:
-----------------------------
These guarantee visibility:

1. synchronized block
   - Exiting synchronized → Entering synchronized (same lock)

2. volatile writes/reads
   - volatile write → volatile read (any)

3. start() → run()
   - Thread.start() → Thread.run()

4. Memory clean-up → join() returns
   - Thread completion → Thread.join() returns

5. Executor submit → task execution
   - executor.submit() → task runs

6. CompletableFuture completion
   - complete() → thenApply() executes


================================================================================
QUICK DECISION TREE
================================================================================

Need to run code in parallel?
├─ Yes, simple one-off? → new Thread() or executor.submit()
├─ Yes, multiple tasks? → ExecutorService
│
Need return value from async code?
├─ Yes, simple? → Future + executor.submit()
├─ Yes, complex pipeline? → CompletableFuture
│
Need to share state between threads?
├─ Simple counter? → AtomicInteger
├─ Many readers, few writers? → ReadWriteLock
├─ Critical section? → synchronized or ReentrantLock
│
Need millions of threads?
├─ Yes, Java 19+? → Virtual Threads
├─ No, Java 17? → ExecutorService with thread pool
│
Need to wait for I/O?
├─ Yes? → Callable, Future, or CompletableFuture
├─ Or Virtual Thread
│
Need to coordinate threads?
├─ Producer-Consumer? → BlockingQueue or wait/notify
├─ Phased execution? → CyclicBarrier
├─ Single event? → CountDownLatch
├─ Thread pool control? → Semaphore

================================================================================

