================================================================================
MULTITHREADING VISUAL GUIDE & DIAGRAMS
================================================================================

================================================================================
1. THREAD LIFECYCLE
================================================================================

                   new Thread()
                        ↓
                    [NEW STATE]
                        ↓
                    .start()
                        ↓
             [RUNNABLE] ←──────→ [RUNNING]
                ↓                    ↓
           .wait()            .sleep(), .join(),
           .park()            I/O blocking
                ↓                    ↓
             [WAITING] ←──────→ [TIMED_WAITING]
                ↓                    ↓
           .notify()          Timeout expired
                ↓                    ↓
             [RUNNABLE]         [RUNNABLE]
                ↓
            Run completed or .stop()
                ↓
            [TERMINATED]


================================================================================
2. RACE CONDITION PROBLEM
================================================================================

UNSAFE CODE: public void increment() { count++; }

Timeline of Race Condition:
===========================

Time  Thread A                Thread B              count
----  --------                --------              -----
 1    Read count (5)
 2                           Read count (5)          5
 3    Increment (5→6)
 4                           Increment (5→6)         5
 5    Write 6                                        6
 6                           Write 6                 6  ← WRONG! Should be 7

Expected: Both increment, final = 7
Actual: Both read same value, final = 6

SAFE CODE: public synchronized void increment() { count++; }

Timeline with Synchronization:
==============================

Time  Thread A                Thread B              count
----  --------                --------              -----
 1    Acquire lock
 2    Read count (5)          [Waiting for lock]     5
 3    Increment (5→6)
 4    Write 6
 5    Release lock
 6                           Acquire lock           6
 7                           Read count (6)
 8                           Increment (6→7)
 9                           Write 7
10                           Release lock            7  ← CORRECT!


================================================================================
3. SYNCHRONIZED BLOCK VISUALIZATION
================================================================================

Object with Intrinsic Lock:
===========================

    ┌─────────────────────────┐
    │   Object Instance       │
    │  ┌─────────────────────┐│
    │  │  Intrinsic Lock     ││  Only ONE thread can hold lock
    │  │  (Monitor)          ││  Others must wait
    │  └─────────────────────┘│
    │  ┌─────────────────────┐│
    │  │  Shared Data        ││  Protected by lock
    │  │  (critical section) ││
    │  └─────────────────────┘│
    └─────────────────────────┘

Multiple Threads Contending for Lock:
=====================================

Lock: [ ]

Thread A: synchronized(obj) {
              ↓
          Lock acquired: [A holds]
          Execute critical section
          ↓
          Lock released: [ ]
          }

Thread B: synchronized(obj) {
              ↓
          [Waiting for lock]
              ↓
          Lock acquired: [B holds]
          Execute critical section
          ↓
          Lock released: [ ]
          }

Thread C: synchronized(obj) {
              ↓
          [Waiting for lock]
              ↓
          ... continues ...
          }


================================================================================
4. THREAD POOL & EXECUTOR FLOW
================================================================================

FIXED THREAD POOL (3 threads):

┌────────────────────────────────────────────────┐
│ ExecutorService (Thread Pool)                  │
│                                                │
│ ┌───────┐  ┌───────┐  ┌───────┐              │
│ │Thrd 1 │  │Thrd 2 │  │Thrd 3 │              │
│ │(idle) │  │(idle) │  │(busy) │              │
│ └──┬────┘  └──┬────┘  └──┬────┘              │
│    │          │          │                   │
│    └──────────┼──────────┘                   │
│               ↓                              │
│    ┌─────────────────────┐                  │
│    │  Task Queue         │                  │
│    │  [T1][T2][T3][T4]   │                  │
│    └─────────────────────┘                  │
│                                               │
└────────────────────────────────────────────────┘

Workflow:
1. Submit Task T1 → Thread 1 picks it up
2. Submit Task T2 → Thread 2 picks it up  
3. Submit Task T3 → Thread 3 picks it up
4. Submit Task T4 → Goes to queue (all threads busy)
5. Thread 1 finishes → Picks up Task T4 from queue


================================================================================
5. CALLABLE & FUTURE TIMELINE
================================================================================

                   Main Thread
                        ↓
              executor.submit(Callable)
                        ↓
              Future<Integer> f = ...
                        ↓
              (Do other work...)
                        ↓
              f.isDone()? [NO]
                        ↓
              result = f.get() ← BLOCKS HERE
                        ↓
              Meanwhile, background thread executes Callable:
              ┌─ compute() → [1000ms of work] → return result
                        ↓
              f.get() returns ← UNBLOCKS
                        ↓
              Use result...


With Timeout:
=============
              try {
                  result = f.get(500, MILLISECONDS)
              } catch (TimeoutException) {
                  f.cancel(true)
              }

Timeline:
        Main waits for result...
        [500ms pass...]
        TimeoutException thrown
        Task cancelled


================================================================================
6. COMPLETABLE FUTURE CHAIN
================================================================================

                   Source
                      ↓
            supplyAsync(getData)
                      ↓
        [Thread Pool] executes async
                      ↓
            thenApply(transform)
                      ↓
        Takes result from previous stage
        Applies transformation
                      ↓
            thenCompose(anotherAsync)
                      ↓
        Flattens nested async operations
                      ↓
            thenAccept(consume)
                      ↓
        Final result consumed
                      ↓
            .exceptionally(handleError)
                      ↓
        If any stage fails, catch and recover


VISUAL EXAMPLE:
===============

CompletableFuture.supplyAsync(
    () -> fetchUser(1)        ← Async, returns User
)
.thenApply(
    user -> user.getSalary()  ← Transform, returns Integer
)
.thenCompose(
    salary -> CompletableFuture.supplyAsync(
        () -> calculateBonus(salary)  ← Async, returns Double
    )
)
.thenAccept(
    bonus -> System.out.println(bonus)  ← Consume result
)
.exceptionally(
    ex -> {                    ← Handle error
        log(ex);
        return null;
    }
);

Stage 1: User created ──→ fetch takes 1s
Stage 2: Int salary   ──→ transform takes 0.1s
Stage 3: Double bonus ──→ calculate takes 0.5s
Stage 4: Print        ──→ consume takes 0.01s
===================================================
Total: ~1.6s (mostly sequential, but all async)


================================================================================
7. VIRTUAL THREADS vs PLATFORM THREADS
================================================================================

PLATFORM THREADS (1:1 with OS threads):
=======================================

┌─────────────────────────────────────────┐
│ Java Process                            │
│                                         │
│ Thread 1 ────────→ [OS Kernel Thread]   │
│ Thread 2 ────────→ [OS Kernel Thread]   │
│ Thread 3 ────────→ [OS Kernel Thread]   │
│ ...                                     │
│ Thread N ────────→ [OS Kernel Thread]   │
│ (MAX ~1000 threads, heavy resources)    │
└─────────────────────────────────────────┘


VIRTUAL THREADS (Many:1 with OS threads):
==========================================

┌──────────────────────────────────────────────┐
│ Java Process (JVM)                           │
│                                              │
│ ┌─────────────────────────────────────────┐  │
│ │ Virtual Thread 1                        │  │
│ │ Virtual Thread 2                        │  │
│ │ Virtual Thread 3                        │  │
│ │ ...                                     │  │
│ │ Virtual Thread 10000                    │  │
│ │ (can have millions!)                    │  │
│ └──────────────┬──────────────────────────┘  │
│                ↓                             │
│   [JVM Scheduler - Maps to Carrier Threads]  │
│                ↓                             │
│ ┌────────────────────────────────────────┐   │
│ │ Carrier Thread 1 ──→ [OS Thread]       │   │
│ │ Carrier Thread 2 ──→ [OS Thread]       │   │
│ │ Carrier Thread 3 ──→ [OS Thread]       │   │
│ │ Carrier Thread 4 ──→ [OS Thread]       │   │
│ │ (small fixed pool)                     │   │
│ └────────────────────────────────────────┘   │
└──────────────────────────────────────────────┘

Advantages:
- Millions of virtual threads vs thousands of platform threads
- Low memory overhead
- Automatic context switching by JVM
- Great for I/O-bound operations


================================================================================
8. PRODUCER-CONSUMER PATTERN
================================================================================

Buffer State Machine:
====================

           [EMPTY]
            ↙   ↘
       producer   consumer
       puts       [blocked]
       value
            ↓
        [FULL]
        ↙   ↘
     consumer  producer
     gets      [blocked]
     value
        ↓
    [EMPTY]

Timeline with wait/notify:
==========================

Time  Producer              Consumer              Buffer
----  --------              --------              ------
 1    put(10)               wait()                [10]
 2    notify()              [woken]
 3    [blocked]             get() returns 10      [empty]
 4                          notify()
 5    [woken]               put(20) next?         [20]
 6    put(20)               [blocked]
 7    notify()              [woken]
 8    [blocked]             get() returns 20      [empty]


================================================================================
9. LOCK CONTENTION
================================================================================

High Contention (Bad Performance):
==================================

Lock: [Locked]
  ├─ Thread A: waiting...
  ├─ Thread B: waiting...
  ├─ Thread C: waiting...
  ├─ Thread D: waiting...
  └─ Thread E: waiting...

Context switching overhead high
CPU cache thrashing
Poor scalability

Low Contention (Good Performance):
===================================

Lock: [Locked]
└─ Thread A: has lock
  Thread B: [continues independent work]
  Thread C: [continues independent work]
  Thread D: [continues independent work]
  Thread E: [continues independent work]

Minimal context switching
Better CPU cache utilization
Scales well


================================================================================
10. EXECUTION COMPARISON
================================================================================

SEQUENTIAL EXECUTION (No Threading):
====================================
Task A: [████████] 1 second
Task B:           [████████] 1 second
Task C:                     [████████] 1 second
────────────────────────────────────────────
Total: 3 seconds


THREADED EXECUTION (3 Threads):
================================
Task A: [████████]
Task B: [████████]
Task C: [████████]
────────────────────
Total: ~1 second (3x speedup!)


VIRTUAL THREADS (10000 Threads):
=================================
Task 1:     [██] 100ms
Task 2:     [██] 100ms
Task 3:     [██] 100ms
...
Task 10000: [██] 100ms
────────────────────────────────────
Total: ~100ms (but 10000 concurrent I/O ops!)
This is only possible with virtual threads
Platform threads would hang!


================================================================================
11. COMPLETABLE FUTURE EXCEPTION HANDLING
================================================================================

Normal Path:
============
supplyAsync(data)
    ↓
    ✓ Success
    ↓
thenApply(transform)
    ↓
    ✓ Success
    ↓
thenAccept(consume)
    ↓
    ✓ Complete


Exception Path:
===============
supplyAsync(data)
    ↓
    ✗ Exception thrown
    ↓
[Pipeline broken]
    ↓
exceptionally(recover)
    ↓
    ✓ Recovery value provided
    ↓
    ✓ Complete with recovery


With handle():
==============
supplyAsync(data)
    ↓
handle((result, exception) → {
    if (exception != null) {
        return fallbackValue;
    }
    return process(result);
})
    ↓
    ✓ Always completes


================================================================================
12. MEMORY VISIBILITY WITH SYNCHRONIZATION
================================================================================

WITHOUT Synchronization:
========================

Thread A                          Thread B
┌─────────────┐                 ┌─────────────┐
│Local Cache  │                 │Local Cache  │
│ count = 0   │                 │ count = 0   │
└─────────────┘                 └─────────────┘
       ↓                               ↓
[Thread A increments locally]   [Thread B increments locally]
       ↓                               ↓
│ count = 1   │                 │ count = 1   │
└─────────────┘                 └─────────────┘

Shared Memory (heap): count = ???
(Both see stale data!)


WITH Synchronization:
=====================

Thread A                          Thread B
┌─────────────┐                 ┌─────────────┐
│Local Cache  │                 │Local Cache  │
│count = 0    │                 │count = ?    │
└─────────────┘                 └─────────────┘
       ↓                               ↓
[Thread A acquires lock]
       ↓
[synchronize flushes cache]
[read count = 0 from memory]
[increment to 1]
[write 1 to memory]
[synchronize flushes cache]
[release lock]
       ↓                               ↓
                               [Thread B acquires lock]
                                       ↓
                            [synchronize flushes cache]
                            [read count = 1 from memory]
                            [increment to 2]
                            [write 2 to memory]
                            [synchronize flushes cache]
                            [release lock]

Shared Memory (heap): count = 2 ✓
(Both threads see current data!)


================================================================================
13. TASK EXECUTION IN DIFFERENT EXECUTOR TYPES
================================================================================

newFixedThreadPool(3):
======================
Task1 ──→ [Thread A]
Task2 ──→ [Thread B]
Task3 ──→ [Thread C]
Task4 ──→ [Queue] ──→ [Waits for Thread A]
Task5 ──→ [Queue] ──→ [Waits for Thread B]
Result: Predictable, bounded resource usage


newCachedThreadPool():
======================
Task1 ──→ [Thread A - New]
Task2 ──→ [Thread B - New]
Task3 ──→ [Thread C - New]
[Idle for 60s]
[Threads 1,2,3 removed]
Task4 ──→ [Thread D - New]
Result: Flexible for variable load, but can create many threads


newSingleThreadExecutor():
==========================
Task1 ──→ [Thread A]
Task2 ──→ [Queue]
Task3 ──→ [Queue]
[All execute sequentially in order]
Result: Guaranteed ordering, but slow


newScheduledThreadPool(2):
==========================
Task1: now ──→ [Thread A]
Task2: now ──→ [Thread B]
Task3: 5s delay ──→ [Queue] ──→ [Thread A] at 5s
Task4: repeating every 2s ──→ [Thread B]
       │       │       │
Result: Scheduled execution possible


================================================================================
14. VIRTUAL THREAD SCHEDULING
================================================================================

Virtual Thread Execution:

Main Process
    ↓
VirtualThread.start()
    ↓
[VirtualThread 1 ──→ IO operation (blocks)]
    ↓
[JVM detects blocking]
    ↓
[Context switch to VirtualThread 2]
    ↓
[VirtualThread 2 ──→ CPU-bound work]
    ↓
[VirtualThread 1 ← IO complete]
    ↓
[Resume VirtualThread 1]
    ↓
[Continue work]

All on same Carrier (OS) thread! No thread creation overhead!

With 10000 Virtual Threads:
- Only 8-16 Carrier threads (by default = # CPU cores)
- Automatic context switching by JVM
- Millions can be created with minimal resources

================================================================================

