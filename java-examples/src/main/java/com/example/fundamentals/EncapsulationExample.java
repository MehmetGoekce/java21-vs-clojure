package com.example.fundamentals;

/**
 * Demonstrates encapsulation in Java 21
 * Encapsulation hides internal state and requires access through methods
 */
public class EncapsulationExample {

    // Class with encapsulated state
    static class BankAccount {
        private final String accountNumber;
        private double balance;
        private final String owner;
        private boolean frozen;

        // Constructor
        public BankAccount(String accountNumber, String owner, double initialDeposit) {
            this.accountNumber = accountNumber;
            this.balance = initialDeposit;
            this.owner = owner;
            this.frozen = false;

        }

        // Getters
        public String getAccountNumber() {
            return accountNumber;
        }

        public String getOwner() {
            return owner;
        }

        public double getBalance() {
            return balance;
        }

        public boolean isFrozen() {
            return frozen;
        }

        //Methods that modify state with validation
        public void deposit(double amount) {
            if (frozen) {
                throw new IllegalStateException("Account is frozen");
            }

            if (amount <= 0) {
                throw new IllegalArgumentException("Deposit amount must be positive");
            }

            this.balance += amount;
            System.out.println(STR."\{amount} deposited. New balance: \{this.balance}");
        }

        public void withdraw(double amount) {
            if (frozen) {
                throw new IllegalStateException("Account is frozen");
            }

            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }

            if (amount > balance) {
                throw new IllegalArgumentException("Insuffucient funds");
            }

            this.balance -= amount;
            System.out.println(STR."\{amount} withdrawn. New balance: \{this.balance}");
        }

        public void freezeAccount() {
            this.frozen = true;
            System.out.println("Account frozen");
        }

        public void unfreezeAccount() {
            this.frozen = false;
            System.out.println("Account unfrozen");
        }

        @Override
        public String toString() {
            return STR."Account[\{accountNumber}] owned by \{owner} with balance: $\{balance}\{frozen ? " (FROZEN)" : ""}";
        }
    }

    // Example usage
    public static void main(String[] args) {
        System.out.println("=== Encapsulation Example ===");

        // Create a new bank account
        BankAccount account = new BankAccount("12345", "John Doe", 1000.0);
        System.out.println(STR."Initial account: \{account}");

        account.deposit(500.0);
        account.withdraw(200.0);

        System.out.println(STR."Account owner: \{account.getOwner()}");
        System.out.println(STR."Current balance: $\{account.getBalance()}");

        // Demonstrate state protection
        try {
            account.withdraw(2000.0); // Should fail
        } catch (IllegalArgumentException e) {
            System.out.println(STR."Protected operation: \{e.getMessage()}");
        }

        // Demonstrate state change
        account.freezeAccount();

        try {
            account.deposit(100.0); // Should fail because account is frozen
        } catch (IllegalStateException e){
            System.out.println(STR."Protected operation: \{e.getMessage()}");
        }

        account.unfreezeAccount();
        account.deposit(100.0);

        System.out.println(STR."Final account: \{account}");
    }
}
