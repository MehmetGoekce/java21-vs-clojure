(ns clojure-examples.dataprocessing
  (:require [java-time :as t]))

;;; Data Processing and Transformation in Clojure
;;; Demonstrates functional data transformation patterns

(defn total-amount
  "Calculate the total amount for a sales record"
  [record]
  (* (:price record) (:quantity record)))

(defn analyze-sales
  "Analyze sales data using functional transformations"
  [records]
  (let [;; Step 1: Filter out invalid records
        valid-records (filter :valid? records)

        ;; Step 2 & 3: Group by category and compute statistics
        by-category (group-by :category valid-records)

        ;; Step 4: Create summary for each category
        summaries (map (fn [[category items]]
                         {:category category
                          :total-sales (reduce + (map total-amount items))
                          :average-price (double (/ (reduce + (map :price items))
                                                    (count items)))
                          :total-products (count items)})
                       by-category)

        ;; Step 5: Sort by total sales (descending)
        sorted-summaries (reverse (sort-by :total-sales summaries))]

    sorted-summaries))

;; Format results for display
(defn format-summary
  "Format summary with robust handling of numeric values"
  [{:keys [category total-sales average-price total-products]}]
  (format "Category: %s, Total Sales: CHF%.2f, Avg Price: CHF%.2f, Products Sold: %d"
          category
          (double total-sales)
          (double average-price)
          total-products))

;; Advanced Analysis - time-based sales analysis
(defn analyze-monthly-sales
  "Analyze sales data by month"
  [records]
  (let [;; Filter valid records
        valid-records (filter :valid? records)

        ;; Extract year and month from date and add as keys
        records-with-month (map (fn [record]
                                  (let [date (:date record)
                                        year (.getYear date)
                                        month (.getMonthValue date)]
                                    (assoc record :year year :month month)))
                                valid-records)

        ;; Group by year and month
        by-year-month (group-by (juxt :year :month) records-with-month)

        ;; Calculate summary for each month
        monthly-summaries (map (fn [[[year month] items]]
                                 {:year year
                                  :month month
                                  :total-sales (double (reduce + (map total-amount items)))
                                  :count (count items)
                                  :top-categories (take 3
                                                        (reverse
                                                          (sort-by second
                                                                   (map first
                                                                        (group-by :category items)))))})
                               by-year-month)

        ;; Sort by year and month
        sorted-summaries (sort-by (juxt :year :month) monthly-summaries)

        ;; Calculate growth rate
        with-growth-rate (map-indexed
                           (fn [idx summary]
                             (if (zero? idx)
                               (assoc summary :growth-rate 0.0)
                               (let [prev-summary (nth sorted-summaries (dec idx))
                                     prev-sales (:total-sales prev-summary)
                                     current-sales (:total-sales summary)
                                     growth-rate (if (pos? prev-sales)
                                                   (* 100.0 (/ (- current-sales prev-sales) prev-sales))
                                                   0.0)]
                                 (assoc summary :growth-rate (double growth-rate)))))
                           sorted-summaries)]
    with-growth-rate))

;; Format monthly summary
(defn format-monthly-summary
  "Format monthly summary with robust numeric handling"
  [{:keys [year month total-sales growth-rate top-categories]}]
  (format "%d-%02d: Sales CHF%.2f, Growth: %.1f%%, Top Categories: %s"
          year month
          (double total-sales)
          (double growth-rate)
          (clojure.string/join ", " top-categories)))

;; Category performance analysis
(defn analyze-category-performance
  "Analyze performance of each product category"
  [records]
  (let [valid-records (filter :valid? records)
        by-category (group-by :category valid-records)
        performance (map (fn [[category items]]
                           (let [total-sales (double (reduce + (map total-amount items)))
                                 avg-price (double (/ (reduce + (map :price items)) (count items)))
                                 total-quantity (reduce + (map :quantity items))
                                 item-count (count items)
                                 items-per-sale (double (/ total-quantity item-count))
                                 avg-item-value (double (/ total-sales total-quantity))]
                             {:category category
                              :total-sales total-sales
                              :average-price avg-price
                              :item-count item-count
                              :total-quantity total-quantity
                              :items-per-sale items-per-sale
                              :avg-item-value avg-item-value}))
                         by-category)]
    (reverse (sort-by :total-sales performance))))

;; Trend analysis function
(defn analyze-trends
  "Analyze trends using a sliding window approach"
  [records window-size]
  (let [valid-records (filter :valid? records)
        ;; Sort records by date
        sorted-records (sort-by :date valid-records)
        ;; Create sliding windows of specified size
        windows (partition window-size 1 sorted-records)
        ;; Analyze each window
        trend-data (map-indexed
                     (fn [idx window]
                       (let [start-date (:date (first window))
                             end-date (:date (last window))
                             total-sales (double (reduce + (map total-amount window)))
                             categories (frequencies (map :category window))
                             top-category (key (apply max-key val categories))]
                         {:window-index idx
                          :start-date start-date
                          :end-date end-date
                          :total-sales total-sales
                          :top-category top-category}))
                     windows)]
    trend-data))

;; Demo usage
(defn -main []
  (let [sales-records [{:id "1" :product "Laptop" :category "Electronics"
                        :price 999.99 :quantity 1 :date (t/local-date 2023 1 15) :valid? true}
                       {:id "2" :product "T-shirt" :category "Clothing"
                        :price 24.99 :quantity 3 :date (t/local-date 2023 1 20) :valid? true}
                       {:id "3" :product "Headphones" :category "Electronics"
                        :price 149.99 :quantity 2 :date (t/local-date 2023 1 25) :valid? true}
                       {:id "4" :product "Book" :category "Media"
                        :price 19.99 :quantity 5 :date (t/local-date 2023 2 5) :valid? true}
                       {:id "5" :product "Smartphone" :category "Electronics"
                        :price 799.99 :quantity 1 :date (t/local-date 2023 2 10) :valid? true}
                       {:id "6" :product "Jeans" :category "Clothing"
                        :price 49.99 :quantity 2 :date (t/local-date 2023 2 15) :valid? true}
                       {:id "7" :product "Tablet" :category "Electronics"
                        :price 349.99 :quantity 1 :date (t/local-date 2023 3 2) :valid? true}
                       {:id "8" :product "Movie" :category "Media"
                        :price 14.99 :quantity 3 :date (t/local-date 2023 3 8) :valid? true}
                       {:id "9" :product "Sweater" :category "Clothing"
                        :price 39.99 :quantity 2 :date (t/local-date 2023 3 15) :valid? true}
                       {:id "10" :product "Invalid Item" :category "Unknown"
                        :price 0.0 :quantity 0 :date (t/local-date 2023 3 20) :valid? false}]]

    (println "Sales Analysis Results:")
    (doseq [summary (analyze-sales sales-records)]
      (println (format-summary summary)))

    (println "\nMonthly Sales Analysis:")
    (doseq [summary (analyze-monthly-sales sales-records)]
      (println (format-monthly-summary summary)))

    (println "\nCategory Performance Analysis:")
    (doseq [perf (analyze-category-performance sales-records)]
      (println (format "Category: %s, Total Sales: CHF%.2f, Avg Item Value: CHF%.2f, Items per Sale: %.1f"
                       (:category perf)
                       (:total-sales perf)
                       (:avg-item-value perf)
                       (:items-per-sale perf))))

    (println "\nTrend Analysis (Window Size 3):")
    (doseq [trend (analyze-trends sales-records 3)]
      (println (format "Window %d: %s to %s, Sales: CHF%.2f, Top Category: %s"
                       (:window-index trend)
                       (:start-date trend)
                       (:end-date trend)
                       (:total-sales trend)
                       (:top-category trend))))))

;; Example REPL usage
(comment
  (-main)
  ;; => "Sales Analysis Results:"
  ;; => "Category: Electronics, Total Sales: CHF1299.97, Avg Price: CHF574.99, Products Sold: 2"
  ;; => "Category: Media, Total Sales: CHF99.95, Avg Price: CHF19.99, Products Sold: 1"
  ;; => "Category: Clothing, Total Sales: CHF74.97, Avg Price: CHF24.99, Products Sold: 1"

  ;; Try your own analysis here
  (analyze-sales [])
  )