package com.example.patterns;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * Demonstrates the Observer Pattern in Java 21
 * The Observer Pattern defines a one-to-many dependency between objects so that
 * when one object changes state, all its dependents are notified and updated automatically.
 */
public class ObserverPatternExample {

    // Traditional Observer Pattern

    // Subject interface
    interface Subject {
        void registerObserver(Observer observer);
        void removeObserver(Observer observer);
        void notifyObservers();
    }

    // Observer interface
    interface Observer {
        void update(String stockSymbol, double price);
    }

    // Concrete Subject
    static class StockMarket implements Subject {
        private final List<Observer> observers = new ArrayList<>();
        private final Map<String, Double> stocks = new ConcurrentHashMap<>();

        @Override
        public void registerObserver(Observer observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(Observer observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (Map.Entry<String, Double> entry : stocks.entrySet()) {
                String symbol = entry.getKey();
                double price = entry.getValue();
                for (Observer observer : observers) {
                    observer.update(symbol, price);
                }
            }
        }

        public void updateStockPrice(String symbol, double price) {
            stocks.put(symbol, price);
            System.out.println(STR."Stock price updated: \{symbol} = CHF\{price}");
            notifyObservers();
        }
    }

    //Concrete Observer
    static class StockDisplay implements Observer {
        private final String name;

        public StockDisplay(String name) {
            this.name = name;
        }

        @Override
        public void update(String stockSymbol, double price) {
            System.out.println(STR."\{name}display: Stock \{stockSymbol}update price: CHF\{price}");
        }
    }

    // Conrete Observer with specific interest
    static class StockAlert implements Observer {
        private final Map<String, Double> thresholds = new ConcurrentHashMap<>();

        public void setAlertThreshold(String symbol, double threshold) {
            thresholds.put(symbol, threshold);
        }

        public void update(String stockSymbol, double price) {
            if (thresholds.containsKey(stockSymbol)) {
                double threshold = thresholds.get(stockSymbol);
                if (price > threshold) {
                    System.out.println(STR."ALERT: Stock \{stockSymbol} exceeded threshold of CHF\{threshold} with current price: CHF\{price}");
                }
            }
        }
    }

    // Java 9+ Reactive Streams approach to Observer Pattern

    // Event Class to be published
    static record StockEvent(String symbol, double price, long timestamp) {}

    // Subscriber implmentation
    static class StockSubscriber implements Flow.Subscriber<StockEvent>{
        private final String name;
        private Flow.Subscription subscription;

        public StockSubscriber(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
            System.out.println(STR."\{name} subscribed to stock events");
        }

        @Override
        public void onNext(StockEvent event) {
            System.out.println(STR."\{name} received: \{event.symbol()} price: $\{event.price()}");
            subscription.request(1); // Request next item
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println(STR."\{name} error: \{throwable.getMessage()}");
        }

        @Override
        public void onComplete() {
            System.out.println(STR."\{name} subscription complete");
        }
    }

    // Functional approach to Observer pattern
    static class FunctionalStockMarket {
        private final List<Consumer<StockEvent>> listeners = new ArrayList<>();

        public void addListener(Consumer<StockEvent> listener) {
            listeners.add(listener);
        }

        public void removeListener(Consumer<StockEvent> listener) {
            listeners.remove(listener);
        }

        public void updateStockPrice(String symbol, double price) {
            StockEvent event = new StockEvent(symbol, price, System.currentTimeMillis());
            System.out.println(STR."Functional approach - Stock price updated: \{symbol} = CHF\{price}");
            listeners.forEach(listener -> listener.accept(event));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Observer Pattern Example ===");

        // Traditional Observer Pattern demo
        System.out.println("\n--- Traditional Observer Pattern ---");
        StockMarket stockMarket = new StockMarket();

        StockDisplay display1 = new StockDisplay("Mobile App");
        StockDisplay display2 = new StockDisplay("Web Dashboard");
        StockAlert alert = new StockAlert();

        alert.setAlertThreshold("NESN", 125.0);
        alert.setAlertThreshold("NOVN", 90.0);


        stockMarket.registerObserver(display1);
        stockMarket.registerObserver(display2);
        stockMarket.registerObserver(alert);

        stockMarket.updateStockPrice("NESN", 120.50);  // Nestlé
        stockMarket.updateStockPrice("NOVN", 85.75);   // Novartis
        stockMarket.updateStockPrice("ROG", 350.20);   // Roche
        stockMarket.updateStockPrice("UBSG", 15.30);   // UBS Group
        stockMarket.updateStockPrice("CSGN", 10.50);   // Credit Suisse Group

        // Clean up
        stockMarket.removeObserver(display2);
        System.out.println("\nAfter removing Web Dashboard observer:");
        stockMarket.updateStockPrice("NESN", 126.35);

        // Java 9+ Reactive Streams demo
        System.out.println("\n--- Java Reactive Streams Observer Pattern ---");
        try (SubmissionPublisher<StockEvent> publisher = new SubmissionPublisher<>()) {
            // Create subscribers
            publisher.subscribe(new StockSubscriber("Financial Advisor"));
            publisher.subscribe(new StockSubscriber("Portfolio Manager"));

            // Publish events
            publisher.submit(new StockEvent("NESN", 121.75, System.currentTimeMillis()));
            publisher.submit(new StockEvent("NOVN", 86.25, System.currentTimeMillis()));

            // Give some time for asynchronous processing
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Functional approach demo
        System.out.println("\n--- Functional Observer Pattern ---");
        FunctionalStockMarket functionalMarket = new FunctionalStockMarket();

        // Add lambda observers
        functionalMarket.addListener(event ->
                System.out.println(STR."Analytics observer: \{event.symbol()} at CHF\{event.price()}")
        );

        functionalMarket.addListener(event -> {
            if (event.price() > 300.0) {
                System.out.println(STR."High-value stock alert: \{event.symbol()} at CHF\{event.price()}");
            }
        });

        functionalMarket.updateStockPrice("ROG", 355.50);
        functionalMarket.updateStockPrice("UBSG", 15.80);

        /*
         * Key Observer Pattern concepts demonstrated:
         * 1. Traditional Observer Pattern with Subject and Observer interfaces
         * 2. Modern implementation using Java's Reactive Streams API
         * 3. Functional approach using Consumer interface
         * 4. Different types of observers with varied behaviors
         * 5. Multiple observers for a single subject
         */
    }

}
