# Java 21 OOP vs Clojure FP

This repository contains code examples demonstrating the differences between Object-Oriented Programming in Java 21 and Functional Programming in Clojure.

## Overview

The code examples in this repository accompany the article "Object-Oriented Programming in Java 21 vs Functional Programming in Clojure: A Technical Comparison". The examples illustrate the fundamental differences in approach, paradigm, and implementation between these two languages and programming styles.

## Repository Structure

```
├── README.md                 # This file
├── LICENSE                   # MIT License
├── java-examples/            # Java 21 examples
│   ├── pom.xml               # Maven project configuration
│   └── src/
│       └── main/java/
│           ├── fundamentals/ # OOP principles examples
│           ├── patterns/     # Design patterns
│           ├── concurrency/  # Virtual threads examples
│           ├── dataprocessing/ # Data transformation examples
│           ├── webscraper/   # Concurrent web scraper
│           └── ecommerce/    # System design example
├── clojure-examples/         # Clojure examples
│   ├── project.clj           # Leiningen project configuration
│   └── src/
│       └── clojure_examples/
│           ├── fundamentals.clj # FP principles examples
│           ├── patterns.clj     # Functional design patterns
│           ├── concurrency.clj  # Concurrency examples
│           ├── dataprocessing.clj # Data transformation examples
│           ├── webscraper.clj   # Concurrent web scraper
│           └── ecommerce.clj    # System design example
└── docs/
    └── article.md            # The full comparison article
```

## Requirements

### Java Examples
- Java 21 or higher
- Maven 3.8+

### Clojure Examples
- Clojure 1.11 or higher
- Leiningen 2.9+
- Java 11+ (for JVM)

## Getting Started

### Running Java Examples

```bash
cd java-examples
mvn compile
mvn exec:java -Dexec.mainClass="com.example.dataprocessing.SalesAnalyzer"
```

### Running Clojure Examples

```bash
cd clojure-examples
lein run -m clojure-examples.dataprocessing
```

## Key Comparison Points

The examples demonstrate the following comparison points:

1. **Fundamental Paradigms**
    - OOP principles in Java (encapsulation, inheritance, polymorphism)
    - FP principles in Clojure (pure functions, immutability, first-class functions)

2. **Code Structure Approaches**
    - State management and data transformation
    - Error/exception handling
    - Concurrency and parallelism
    - Design patterns implementation
    - API design

3. **Implementations of Common Problems**
    - Data processing with transformations
    - Concurrent web scraping
    - E-commerce system design

4. **Technical Considerations**
    - Memory usage and garbage collection
    - Performance characteristics
    - Testability approaches
    - Interoperability strategies

5. **Language-Specific Features**
    - Java's virtual threads vs Clojure's concurrency
    - Java's records vs Clojure's maps
    - Java's pattern matching vs Clojure's destructuring
    - Java's functional interfaces vs Clojure's function composition

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.