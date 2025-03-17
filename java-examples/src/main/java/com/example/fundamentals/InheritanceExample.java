package com.example.fundamentals;

/**
 * Demonstrates inheritance in Java 21 with German fuel efficiency metrics
 * Inheritance allows classes to reuse and extend behavior from parent classes
 */
public class InheritanceExample {

    // Base class
    static class Vehicle {
        private final String make;
        private final String model;
        private final int year;

        public Vehicle(String make, String model, int year) {
            this.make = make;
            this.model = model;
            this.year = year;
        }

        public String getMake() {
            return make;
        }

        public String getModel() {
            return model;
        }

        public int getYear() {
            return year;
        }

        public void startEngine() {
            System.out.println("Vehicle engine started");
        }

        public void stopEngine() {
            System.out.println("Vehicle engine stopped");
        }

        public double calculateFuelEfficiency() {
            return 0.0; // Base implementation, in L/100km
        }

        @Override
        public String toString() {
            return STR."\{year} \{make} \{model}";
        }
    }

    // Child class
    static class Car extends Vehicle {
        private final int numDoors;
        private final boolean isElectric;

        public Car(String make, String model, int year, int numDoors, boolean isElectric) {
            super(make, model, year); // Call parent constructor
            this.numDoors = numDoors;
            this.isElectric = isElectric;
        }

        public int getNumDoors() {
            return numDoors;
        }

        public boolean isElectric() {
            return isElectric;
        }

        // Override parent's method
        @Override
        public void startEngine() {
            if (isElectric) {
                System.out.println("Car silently powered on");
            } else {
                System.out.println("Car engine started with a roar");
            }
        }

        @Override
        public double calculateFuelEfficiency() {
            return isElectric ? 15.0 : 7.8; // kWh/100km for electric, L/100km for gas
        }

        // Method specific to Car
        public void activateChildLock() {
            System.out.println("Child locks activated");
        }

        @Override
        public String toString() {
            return STR."\{super.toString()} - \{numDoors} door\{isElectric ? " electric" : " gas"} car";
        }
    }

    // Derived class
    static class Motorcycle extends Vehicle {
        private final boolean hasSidecar;

        public Motorcycle(String make, String model, int year, boolean hasSidecar) {
            super(make, model, year);
            this.hasSidecar = hasSidecar;
        }

        public boolean hasSidecar() {
            return hasSidecar;
        }

        @Override
        public void startEngine() {
            System.out.println("Motorcycle engine started with a buzz");
        }

        @Override
        public double calculateFuelEfficiency() {
            return hasSidecar ? 5.9 : 4.7; // L/100km changes with sidecar
        }

        // Method specific to Motorcycle
        public void doWheelie() {
            if (hasSidecar) {
                System.out.println("Cannot do a wheelie with a sidecar!");
            } else {
                System.out.println("Doing a wheelie!");
            }
        }

        @Override
        public String toString() {
            return STR."\{super.toString()} - motorcycle\{hasSidecar ? " with sidecar" : ""}";
        }
    }

    // Demo
    public static void main(String[] args) {
        System.out.println("=== Inheritance Example ===");

        // Create instances of base and derived classes
        Vehicle genericVehicle = new Vehicle("Generic", "Transport", 2023);
        Car limousine = new Car("Volkswagen", "Golf", 2022, 4, false);
        Car electricCar = new Car("BMW", "i4", 2023, 4, true);
        Motorcycle sportBike = new Motorcycle("BMW", "R 1250 GS", 2021, false);

        // Using base class methods
        System.out.println("\nBase class instance:");
        System.out.println(genericVehicle);
        genericVehicle.startEngine();
        genericVehicle.stopEngine();
        System.out.println(STR."Fuel efficiency: \{genericVehicle.calculateFuelEfficiency()} L/100km");

        // Using inherited and overridden methods
        System.out.println("\nGas car instance:");
        System.out.println(limousine);
        limousine.startEngine(); // Overridden method
        limousine.stopEngine(); // Inherited method
        System.out.println(STR."Fuel efficiency: \{limousine.calculateFuelEfficiency()} L/100km");
        limousine.activateChildLock(); // Car-specific method

        System.out.println("\nElectric car instance:");
        System.out.println(electricCar);
        electricCar.startEngine(); // Overridden method behaves differently
        System.out.println(STR."Fuel efficiency: \{electricCar.calculateFuelEfficiency()} kWh/100km");

        System.out.println("\nMotorcycle instance:");
        System.out.println(sportBike);
        sportBike.startEngine(); // Overridden differently than Car
        sportBike.stopEngine(); // Inherited method
        System.out.println(STR."Fuel efficiency: \{sportBike.calculateFuelEfficiency()} L/100km");
        sportBike.doWheelie(); // Motorcycle-specific method

        /*
         * Key inheritance concepts demonstrated:
         * 1. Base class defines common attributes and behaviors
         * 2. Child classes inherit properties and methods from parent
         * 3. Method overriding allows specialized behavior
         * 4. Child classes can add new properties and methods
         * 5. "super" keyword allows access to parent class members
         */
    }
}