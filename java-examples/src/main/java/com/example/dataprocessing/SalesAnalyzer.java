package com.example.dataprocessing;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates modern data processing in Java 21
 * Making use of records, pattern matching, var, and stream operations
 */

public class SalesAnalyzer {

    public record SalesRecord(
            String id,
            String product,
            String category,
            double price,
            int quantity,
            LocalDate date,
            boolean isValid
    ) {
        public double getTotalAmount() {
            return price * quantity;
        }
    }

    public record CategorySummary(
            String category,
            double totalSales,
            double averagePrice,
            int totalProducts
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Category: %s, Total Sales: CHF%.2f, Avg Price: CHF%.2f, Products Sold: %d",
                    category, totalSales, averagePrice, totalProducts
            );
        }
    }

    public List<CategorySummary> analyzeSales(List<SalesRecord> records) {

        // Step 1: Filter out invalid records
        var validRecords = records.stream()
                .filter(SalesRecord::isValid)
                .toList();

        // Step 2: Group by product category
        var salesByCategory = validRecords.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::category,
                        Collectors.summarizingDouble(SalesRecord::getTotalAmount)

                ));

        // Step 3: Calculate average price per category
        var pricesByCategory = validRecords.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::category,
                        Collectors.averagingDouble(SalesRecord::price)
                ));

        // step 4: Calculate total quantity sold per category
        var quantityByCategory = validRecords.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::category,
                        Collectors.summingInt(SalesRecord::quantity)
                ));

        // Step 5: Create summary and sort by total sales
        return salesByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    DoubleSummaryStatistics stats = entry.getValue();
                    double totalSales = stats.getSum();
                    double avgPrice = pricesByCategory.get(category);
                    int productCount = (int) stats.getCount();

                    return new CategorySummary(category, totalSales, avgPrice, productCount);
                })
                .sorted(Comparator.comparing(CategorySummary::totalSales).reversed())
                .toList();
    }

    // Advanced analytics methods

    public record MonthlySalesReport(
            int year,
            int month,
            double totalSales,
            double growth, // percentage growth from previous month
            List<String> topCategories
    ) {
        @Override
        public String toString() {
            return String.format(
              "%d-%02d: Sales CHF%.2f, Growth: %.1f%%, Top Categories: %s",
              year, month, totalSales, growth, String.join(", ", topCategories)
            );
        }
    }

    public List<MonthlySalesReport> generateMonthlyReports(List<SalesRecord>  records) {

        // Step 1: Group records by year and month
        Map<Integer, Map<Integer, List<SalesRecord>>> salesByYearAndMonth = records.stream()
                .filter(SalesRecord::isValid)
                .collect(Collectors.groupingBy(
                        record -> record.date().getYear(),
                        Collectors.groupingBy(record -> record.date().getMonthValue())
                ));
        // Step 2:  Calculate monthly totals and growth
        List<MonthlySalesReport> reports = new ArrayList<>();
        Map<Integer, Map<Integer, Double>> totalsByYearAndMonth = new HashMap<>();

        // Calculate totals first
        salesByYearAndMonth.forEach((year, monthData) -> {
            totalsByYearAndMonth.put(year, new HashMap<>());
            monthData.forEach((month, monthRecords) -> {
                double total = records.stream()
                        .mapToDouble(SalesRecord::getTotalAmount)
                        .sum();
                totalsByYearAndMonth.get(year).put(month, total);
            });
        });

        // Now generate reports with growth calculation
        salesByYearAndMonth.forEach((year, monthData) -> {
           List<Integer> sortedMonths = new ArrayList<>(monthData.keySet());
           Collections.sort(sortedMonths);

           for (int i = 0; i < sortedMonths.size(); i++) {
               int month = sortedMonths.get(i);
               List<SalesRecord> monthRecords = monthData.get(month);
               double total = totalsByYearAndMonth.get(year).get(month);

               // Calculate growth
               double growth = 0.0;
               if (i > 0) {
                   int prevMonth = sortedMonths.get(i - 1);
                   double prevTotal = totalsByYearAndMonth.get(year).get(prevMonth);
                   growth = prevTotal > 0 ? ((total - prevTotal) / prevTotal) * 100 : 0.0;
               } else if (year > salesByYearAndMonth.keySet().stream().min(Integer::compareTo).orElse(year)) {
                   // Compare with December of previous year if available
                   int prevYear = year - 1;
                   if (totalsByYearAndMonth.containsKey(prevYear) &&
                   totalsByYearAndMonth.get(prevYear).containsKey(12)) {
                       double prevTotal = totalsByYearAndMonth.get(prevYear).get(12);
                       growth = prevTotal > 0 ? ((total - prevTotal) / prevTotal) * 100 : 0.0;
                   }
               }

               // Find top categories for the month
               Map<String, Double> categorySales = monthRecords.stream()
                               .collect(Collectors.groupingBy(
                                       SalesRecord::category,
                                       Collectors.summingDouble(SalesRecord::getTotalAmount)
                               ));
               List<String> topCategories = categorySales.entrySet().stream()
                               .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                       .limit(3)
                                               .map(Map.Entry::getKey)
                                                       .toList();

               reports.add(new MonthlySalesReport(year, month, total, growth, topCategories));

           }
        });

        // Sort by year and month
        return reports.stream()
                .sorted(Comparator.comparing(MonthlySalesReport::year)
                        .thenComparing(MonthlySalesReport::month))
                .toList();
    }

    // Demo usage
    public static void main(String[] args) {
        var salesRecords = List.of(
                new SalesRecord("1", "Laptop", "Electronics", 999.99, 1, LocalDate.of(2023, 1, 15), true),
                new SalesRecord("2", "T-shirt", "Clothing", 24.99, 3, LocalDate.of(2023, 1, 20), true),
                new SalesRecord("3", "Headphones", "Electronics", 149.99, 2, LocalDate.of(2023, 1, 25), true),
                new SalesRecord("4", "Book", "Media", 19.99, 5, LocalDate.of(2023, 2, 5), true),
                new SalesRecord("5", "Smartphone", "Electronics", 799.99, 1, LocalDate.of(2023, 2, 10), true),
                new SalesRecord("6", "Jeans", "Clothing", 49.99, 2, LocalDate.of(2023, 2, 15), true),
                new SalesRecord("7", "Tablet", "Electronics", 349.99, 1, LocalDate.of(2023, 3, 2), true),
                new SalesRecord("8", "Movie", "Media", 14.99, 3, LocalDate.of(2023, 3, 8), true),
                new SalesRecord("9", "Sweater", "Clothing", 39.99, 2, LocalDate.of(2023, 3, 15), true),
                new SalesRecord("10", "Invalid Item", "Unknown", 0.0, 0, LocalDate.of(2023, 3, 20), false)
        );

        var analyzer = new SalesAnalyzer();

        System.out.println("=== Basic Sales Analysis ===");
        var results = analyzer.analyzeSales(salesRecords);
        results.forEach(System.out::println);

        System.out.println("\n=== Monthly Sales Reports ===");
        var monthlyReports = analyzer.generateMonthlyReports(salesRecords);
        monthlyReports.forEach(System.out::println);
    }

}
