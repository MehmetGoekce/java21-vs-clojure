package com.example.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Demonstrates the Strategy Pattern in Java 21
 * The Strategy Pattern defines a family of algorithms,
 * encapsulates each one, and makes them interchangeable.
 */

public class StrategyPatternExample {

    // Strategy interface
    interface SortStrategy {
        <T extends Comparable<T>> List<T> sort(List<T> list);
    }

    // Concrete strategies
    static class QuickSortStrategy implements SortStrategy {
        @Override
        public <T extends Comparable<T>> List<T> sort(List<T> list) {
            // For demo, we'll use Java's built-in sort which uses quicksort variation
            List<T> sortedList = new ArrayList<>(list);
            Collections.sort(sortedList);
            System.out.println("Sorting using Quicksort strategy");
            return sortedList;
        }
    }

    static class MergeSortStrategy implements SortStrategy{
        @Override
        public <T extends Comparable<T>> List<T> sort(List<T> list) {
            // For demo purposes - in reality we would implement mergesort
            List<T> sortedList = new ArrayList<>(list);
            sortedList.sort(Comparator.naturalOrder());
            System.out.println(STR."Sorting using MergeSort strategy");
            return sortedList;
        }
    }

    // Strategy implementation using Java 21 functional interfaces
    static class FunctionalSortStrategy {
        public <T extends Comparable<T>> List<T> sortWith(
                List<T> list, java.util.function.Function<List<T>, List<T>> sortAlgorithm) {
            return sortAlgorithm.apply(list);
        }
    }

    // Context class
    static class Sorter {
        private SortStrategy strategy;

        public void setStrategy(SortStrategy strategy) {
            this.strategy = strategy;
        }

        public <T extends Comparable<T>> List<T> sort(List<T> list) {
            return strategy.sort(list);
        }
    }

    public static void main(String[] args) {
        // Prepare test data
        List<Integer> numbers = List.of(5, 3, 9, 1, 7, 2, 8, 4, 6);

        System.out.println(STR."Original list: \{numbers}");

        // Traditional Strategy Pattern usage
        Sorter sorter = new Sorter();

        // Using Quicksort strategy
        sorter.setStrategy(new QuickSortStrategy());
        List<Integer> quickSorted = sorter.sort(numbers);
        System.out.println(STR."QuickSort result: \{quickSorted}");

        // using MergeSort strategy
        sorter.setStrategy(new MergeSortStrategy());
        List<Integer> mergeSorted = sorter.sort(numbers);
        System.out.println(STR."Mergesort result: \{mergeSorted}");

        // using Functional approach
        System.out.println(STR."""
                Functional Approach:""");
        FunctionalSortStrategy functionalSorter = new FunctionalSortStrategy();

        // using lambda expressions
        List<Integer> functionalQuickSort = functionalSorter.sortWith(
                numbers,
                list -> {
                    List<Integer> result = new ArrayList<>(list);
                    Collections.sort(result);
                    System.out.println(STR."Sorting using functional QuickSort");
                    return result;
                }
        );
        System.out.println(STR."custom sort result: \{functionalQuickSort}");

        // Using method references as strategies
        List<Integer> sortedWithMethodRef = functionalSorter.sortWith(
                numbers,
                StrategyPatternExample::bubbleSort
        );
        System.out.println(STR."Custom sort results: \{sortedWithMethodRef}");
    }

    // Custom sort algorithm for demo
    private static <T extends Comparable<T>> List<T> bubbleSort(List<T> list) {
        List<T> result = new ArrayList<>(list);
        int n = result.size();
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (result.get(j).compareTo(result.get(j+1)) > 0) {
                    T temp = result.get(j);
                    result.set(j, result.get(j+1));
                    result.set(j+1, temp);
                }
            }
        }
        System.out.println(STR."Sorting using custom BubbleSort");
        return result;

        /*
         * The Strategy Pattern in this example demonstrates:
         * Defining a family of algorithms (SortStrategy interface)
         * Encapsulating each algorithm in separate classes (QuickSortStrategy, MergeSortStrategy)
         * Making the algorithms interchangeable at runtime
         * A context class (Sorter) that accepts different strategies
         * Both traditional interface-based and functional approaches using Java 21 features
         */
    }

}
