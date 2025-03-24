package com.example;

import com.example.fundamentals.EncapsulationExample;
import com.example.fundamentals.InheritanceExample;
import com.example.fundamentals.PolymorphismExample;
import com.example.patterns.BuilderPatternExample;
import com.example.patterns.FactoryPatternExample;
import com.example.patterns.ObserverPatternExample;
import com.example.patterns.StrategyPatternExample;
import com.example.webscraper.WebScraper;

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

        System.out.println(STR."""
                === running Design Patterns Examples ===""");
        StrategyPatternExample.main(null);
        BuilderPatternExample.main(null);
        FactoryPatternExample.main(null);
        ObserverPatternExample.main(null);

        System.out.println("\n=== Running Web Scraper Example ===");
        // Limiting scope for faster execution in demo
        List<String> urls = List.of("https://example.com");
        List<String> keywords = List.of("example", "domain");
        new WebScraper().scrapeDemoUrls(urls, keywords);
    }

    private static void runSpecificExample(String exampleName) {
        switch (exampleName.toLowerCase()) {
            case "fundamentals" -> {
                EncapsulationExample.main(null);
                InheritanceExample.main(null);
                PolymorphismExample.main(null);
            }

            case "patterns" -> {
                StrategyPatternExample.main(null);
                BuilderPatternExample.main(null);
                FactoryPatternExample.main(null);
                ObserverPatternExample.main(null);
            }

            case "webscraper" -> {
                List<String> urls = List.of("https://example.com");
                List<String> keywords = List.of("example", "domain");
                new WebScraper().scrapeDemoUrls(urls, keywords);
            }
        }
    }
}
