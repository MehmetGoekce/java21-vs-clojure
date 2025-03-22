package com.example.patterns;

import javax.print.Doc;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Demonstrates the Factory Pattern in Java 21
 * The Factory Pattern provides an interface for creating objects without specifying their concrete classes
 */
public class FactoryPatternExample {

    interface Document {
        void open();
        void save();
        String getType();
    }

    // Concrete product implementations
    static class PDFDocument implements Document {
        @Override
        public void open() {
            System.out.println("Opening PDF document");
        }

        @Override
        public void save() {
            System.out.println("Saving PDF document");
        }

        @Override
        public String getType() {
            return "PDF";
        }
    }

    static class WordDocument implements Document {
        @Override
        public void open() {
            System.out.println("Opening Word document");
        }

        @Override
        public void save() {
            System.out.println("Saving Word document");
        }

        @Override
        public String getType() {
            return "Word";
        }
    }

    static class SpreadsheetDocument implements Document {
        @Override
        public void open() {
            System.out.println("Opening Spreadsheet document");
        }

        @Override
        public void save() {
            System.out.println("Saving Spreadsheet document");
        }

        @Override
        public String getType() {
            return "Spreadsheet";
        }
    }

    // Traditional Factory Method Pattern
    static class DocumentFactory {
        public Document createDocument(String type) {
            return switch (type.toLowerCase()) {
                case "pdf" -> new PDFDocument();
                case "word" -> new WordDocument();
                case "spreadsheet" -> new SpreadsheetDocument();
                default -> throw new IllegalArgumentException(STR."Unknown document type: \{type}");
            };
        }
    }

    // Java 21 Factory Implementation using sealed interface and pattern matching
    sealed interface DocumentType permits PDFType, WordType, SpreadsheetType {}
    record PDFType() implements DocumentType {}
    record WordType() implements DocumentType {}
    record SpreadsheetType() implements DocumentType {}

    static class Modern21DocumentFactory {
        public Document createDocument(DocumentType type) {
            return switch (type) {
                case PDFType ignored -> new PDFDocument();
                case WordType ignored -> new WordDocument();
                case SpreadsheetType ignored -> new SpreadsheetDocument();
            };
        }
    }

    // Factory implementation using functional interfaces
    static class FunctionalDocumentFactory {
        private final Map<String, Supplier<Document>> documentSuppliers = Map.of(
                "pdf", PDFDocument::new,
                "word", WordDocument::new,
                "spreadsheet", SpreadsheetDocument::new
        );

        public Document createDocument(String type) {
            Supplier<Document> supplier = documentSuppliers.get(type.toLowerCase());
            if (supplier == null) {
                throw new IllegalArgumentException(STR."Unknown document type: \{type}");
            }
            return supplier.get();
        }
    }

    // Abstract Factory Pattern example for different document styles
    interface DocumentStyleFactory {
        Document createTextDocument();
        Document createSpreadsheetDocument();
    }

    static class ModernStyleFactory implements DocumentStyleFactory {
        @Override
        public Document createTextDocument() {
            System.out.println("Creating modern style text document");
            return new WordDocument();
        }

        @Override
        public Document createSpreadsheetDocument() {
            System.out.println("Creating modern style spreadsheet");
            return new SpreadsheetDocument();
        }
    }

    static class ClassicStyleFactory implements DocumentStyleFactory {
        @Override
        public Document createTextDocument() {
            System.out.println("Creating classic style text document");
            return new WordDocument(); // In a real example, these would be different implementations
        }

        @Override
        public Document createSpreadsheetDocument() {
            System.out.println("Creating classic style spreadsheet");
            return new SpreadsheetDocument();
        }
    }

    // Client class that uses the factories
    static class DocumentEditor {
        private final DocumentFactory factory;

        public DocumentEditor(DocumentFactory factory) {
            this.factory = factory;
        }

        public void createAndEditDocument(String type) {
            Document doc = factory.createDocument(type);
            System.out.println(STR."Editing a \{doc.getType()} document");
            doc.open();
            doc.save();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Factory Pattern Example ===");

        // Using the traditional Factory Method pattern
        System.out.println(STR."""
                ---Traditional Factory Method ---""");
        DocumentFactory factory = new DocumentFactory();
        Document pdfDoc = factory.createDocument("pdf");
        pdfDoc.open();
        pdfDoc.save();

        Document wordDoc = factory.createDocument("word");
        wordDoc.open();
        wordDoc.save();

        System.out.println(STR."""
                --- Factory with Client --- """);
        DocumentEditor editor = new DocumentEditor(factory);
        editor.createAndEditDocument("spreadsheet");

        // Using the Java 21 factory with sealed interfaces
        System.out.println(STR."""
                --- Java 21 Factory with Sealed Interfaces ---""");
        Modern21DocumentFactory modern21DocumentFactory = new Modern21DocumentFactory();
        Document modernPdfDoc = modern21DocumentFactory.createDocument(new PDFType());
        modernPdfDoc.open();
        modernPdfDoc.save();

        // Using the functional factory
        System.out.println(STR."""
                --- Functional Factory --- """);
        FunctionalDocumentFactory functionalFactory = new FunctionalDocumentFactory();
        Document spreadsheetDoc = functionalFactory.createDocument("spreadsheet");
        spreadsheetDoc.open();
        spreadsheetDoc.save();

        // Using the Abstract Factory pattern
        System.out.println(STR."""
                --- Abstract Factory Pattern""");
        DocumentStyleFactory modernStyleFactory = new ModernStyleFactory();
        Document modernText = modernStyleFactory.createTextDocument();
        modernText.open();

        DocumentStyleFactory classicStyleFactory = new ClassicStyleFactory();
        Document classicSpreadsheet = classicStyleFactory.createSpreadsheetDocument();
        classicSpreadsheet.open();

        /*
         * Key Factory Pattern concepts demonstrated:
         * 1. Factory Method Pattern - createDocument method hides implementation details
         * 2. Abstract Factory Pattern - families of related objects (style-specific documents)
         * 3. Functional approach using Supplier interface
         * 4. Java 21 features: sealed interfaces and pattern matching in switch expressions
         */
    }
}
