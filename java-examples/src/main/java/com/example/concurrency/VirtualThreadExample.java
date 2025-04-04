package com.example.concurrency;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Demonstrates Java 21's Virtual Threads (Project Loom) capabilities
 */
@SuppressWarnings("preview")

public class VirtualThreadExample {

    public static void main(String[] args) {
        System.out.println("=== Virtual Threads Example ===");
        simpleVirtualThreadDemo();
        threadComparison();
        virtualThreadPerTaskExecutorDemo();
    }

    // Simple example of creating and using a virtual thread
    private static void simpleVirtualThreadDemo() {
        System.out.println("\n--- Simple Virtual Thread Demo ---");

        try {
            // Start a virtual thread
            Thread vThread = Thread.startVirtualThread(() -> {
                System.out.println(STR."Running in a virtual thread: \{Thread.currentThread()}");
                try {
                    Thread.sleep(100); // Virtual thread will not block platform thread
                    System.out.println("Virtual thread completed task");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // Wait for the virtual thread to complete
            vThread.join();

            System.out.println("Main thread: Virtual thread has completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Compare platform threads vs virtual threads
    private static void threadComparison() {
        System.out.println("\n--- Platform vs Virtual Threads Comparison ---");

        final int THREAD_COUNT = 10_000;

        // Test with platform threads
        Instant platformStart = Instant.now();
        try (ExecutorService executorService = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int id = i;
                executorService.submit(() -> {
                    try {
                        // Simulate some work with IO waiting
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id;
                });
            }
        }
        Duration platformDuration = Duration.between(platformStart, Instant.now());

        // Test with virtual threads
        Instant virtualStart = Instant.now();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int id = i;
                executorService.submit(() -> {
                    try {
                        // Simulate some work with IO waiting
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id;
                });
            }
        }
        Duration virtualDuration = Duration.between(virtualStart, Instant.now());

        System.out.println(STR."Platform threads execution time: \{platformDuration.toMillis()}ms");
        System.out.println(STR."Virtual threads execution time: \{virtualDuration.toMillis()}ms");
        System.out.println(STR."Virtual threads were \{(double) platformDuration.toMillis() / virtualDuration.toMillis()}x faster!");
    }

    // Demo showing VirtualThreadPerTaskExecutor
    private static void virtualThreadPerTaskExecutorDemo() {
        System.out.println("\n--- Virtual Thread Per Task Executor Demo ---");

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submit multiple tasks that will execute concurrently
            IntStream.range(0, 20).forEach(i -> {
                executor.submit(() -> {
                    try {
                        System.out.println(STR."Task \{i} started on thread: \{Thread.currentThread()}");
                        // Simulate IO-bound work
                        Thread.sleep((long) (Math.random() * 100));
                        System.out.println(STR."Task \{i} completed");
                        return i;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -1;
                    }
                });
            });

            // The executor is automatically closed at the end of the try-with-resources block
            System.out.println("All tasks submitted, waiting for completion...");
        }

        System.out.println("All tasks completed");
    }
}