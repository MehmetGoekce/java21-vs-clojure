package com.example;

import com.example.fundamentals.EncapsulationExample;
import com.example.fundamentals.InheritanceExample;
import com.example.fundamentals.PolymorphismExample;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            runAllExamples();
        } else {
            runSpecificExample(args[0]);
        }
    }

    private static void runAllExamples() {
        System.out.println("\n=== Running OOP Fundamentals Examples ===");
        EncapsulationExample.main(null);
        InheritanceExample.main(null);
        PolymorphismExample.main(null);
    }

    private static void runSpecificExample(String exampleName) {
        switch (exampleName.toLowerCase()) {
            case "fundamentals" -> {
                EncapsulationExample.main(null);
                InheritanceExample.main(null);
                PolymorphismExample.main(null);
            }
        }
    }
}
