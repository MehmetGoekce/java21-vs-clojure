package com.example.fundamentals;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates polymorphism in Java 21
 * Polymorphism allows objects to be treated as instances of their parent class
 */

public class PolymorphismExample {

    // Interface defining a payment processor contract
    interface PaymentProcessor {
        boolean processPayment(double amount);
        String getProcessorName();

        // Default method added to interface
        default String getDescription() {
            return STR."\{getProcessorName()} payment processor";
        }
    }

    // First concrete implementation
    static class CreditCardProcessor implements PaymentProcessor {
        private final String cardNetwork;

        public CreditCardProcessor(String cardNetwork) {
            this.cardNetwork = cardNetwork;
        }

        @Override
        public boolean processPayment(double amount) {
            System.out.println(STR."Processing $\{amount} via \{cardNetwork} credit card ");
            //Simulate processing logic
            return amount <= 5000; // Approve if under limit
        }

        @Override
        public String getProcessorName() {
            return cardNetwork;
        }

        // Method specific to credit cards
        public void verifyCardSecurity() {
            System.out.println(STR."Verifying \{cardNetwork} card security features");
        }
    }

    // Second concrete implementation
    static class PayPalProcessor implements PaymentProcessor {
        private final String email;

        public PayPalProcessor(String email) {
            this.email = email;
        }

        @Override
        public boolean processPayment(double amount) {
            System.out.println(STR."Processing $\{amount} via PayPal account: \{email}");
            // Simulate processing logic
            return amount <= 2000;
        }

        @Override
        public String getProcessorName() {
            return "PayPal";
        }

        // Method specific to PayPal
        public void checkAccountBalance() {
            System.out.println(STR."Checking PayPal account balance for: \{email}");
        }
    }

    // Third concrete implementation
    static class CryptoCurrencyProcessor implements PaymentProcessor {
        private final String currency;

        public CryptoCurrencyProcessor(String currency) {
            this.currency = currency;
        }

        @Override
        public boolean processPayment(double amount) {
            System.out.print(STR."Processing $\{amount} via \{currency} cryptocurrency");
            // Simulate processing logic
            return true; // Always approve for this example
        }

        @Override
        public String getProcessorName() {
            return currency;
        }

        // Override default method
        @Override
        public String getDescription() {
            return STR."Blockchained-based \{currency} payment processor";
        }

        // Method specific to cryptocurrency
        public void verifyBlockchainConfirmation() {
            System.out.println(STR."Verifying \{currency} blockchain confirmation");
        }
    }

    // A class that uses payment processor
    static class PaymentService {
        private List<PaymentProcessor> availableProcessors;

        public PaymentService() {
            this.availableProcessors = new ArrayList<>();
        }

        public void registerProcessor(PaymentProcessor processor) {
            availableProcessors.add(processor);
            System.out.println(STR."Registered: \{processor.getDescription()}");
        }

        public boolean processPayment(double amount, PaymentProcessor processor) {
            System.out.println(STR."""

Attempting payment of $\{amount}""");
            return processor.processPayment(amount);
        }

        public void listAvailableProcessors() {
            System.out.println(STR."""
                    Available payment processors:""");
            for (PaymentProcessor processor : availableProcessors) {
                System.out.println(STR."- \{processor.getDescription()}");;
            }
        }

        // Method demonstrating runtime polymorphism with pattern matching
        public void analyzeProcessor(PaymentProcessor processor) {
            System.out.println(STR."""

Analyzing payment processor: \{processor.getProcessorName()}""");

            // Using switch expressions with type patterns
            switch (processor) {
                case CreditCardProcessor creditCard -> {
                    System.out.println(STR."This is a credit card processor for \{creditCard.getProcessorName()}");
                    creditCard.verifyCardSecurity();
                }
                case PayPalProcessor paypal -> {
                    System.out.println(STR."This is a PayPal processor for account with email: \{paypal.email}");
                    paypal.checkAccountBalance();
                }
                case CryptoCurrencyProcessor crypto -> {
                    System.out.println(STR."This is a cryptocurrency processor for  \{crypto.currency}");
                    crypto.verifyBlockchainConfirmation();
                }
                default -> System.out.println("Unknown processor type");
            }
        }
    }

    // Demo
    public static void main(String[] args) {
        System.out.println("=== Polymorphism Example ===");

        // Create different types of payment processors
        PaymentProcessor visaProcessor = new CreditCardProcessor("Visa");
        PaymentProcessor paypalProcessor = new PayPalProcessor("user@example.com");
        PaymentProcessor bitcoinProcessor = new CryptoCurrencyProcessor("Bitcoin");

        // Create payment service
        PaymentService paymentService = new PaymentService();

        // Register processors
        paymentService.registerProcessor(visaProcessor);
        paymentService.registerProcessor(paypalProcessor);
        paymentService.registerProcessor(bitcoinProcessor);

        // List available processors
        paymentService.listAvailableProcessors();

        // Process payments
        System.out.println(STR."""
                Processing payments:""");
        boolean payment1 = paymentService.processPayment(500.0, visaProcessor);
        System.out.println(STR."Payment \{payment1 ? "approved" : "declined"}");

        boolean payment2 = paymentService.processPayment(5000.0, paypalProcessor);
        System.out.println(STR."Payment \{payment2 ? "approved" : "declined"}");

        boolean payment3 = paymentService.processPayment(50000.0, bitcoinProcessor);
        System.out.println(STR."Payment \{payment3 ? "approved" : "declined"}");

        // Runtime type detection and specific behavior
        paymentService.analyzeProcessor(visaProcessor);
        paymentService.analyzeProcessor(paypalProcessor);
        paymentService.analyzeProcessor(bitcoinProcessor);
    }

    /*
     * Key polymorphism concepts demonstrated:
     * 1. Interface defines a contract for various implementations
     * 2. Different classes implement the same interface
     * 3. Objects can be referenced through their interface type
     * 4. Method calls are resolved at runtime (dynamic dispatch)
     * 5. Switch Expressions with Type Patterns
     * 6. Default methods in interfaces allow interface evolution
     */


}
