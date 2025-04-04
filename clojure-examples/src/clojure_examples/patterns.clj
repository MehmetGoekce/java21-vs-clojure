(ns clojure-examples.patterns
  (:require [clojure.string :as str]))

;; Strategy Pattern in Clojure
;; Functions as strategies

(defn quick-sort
  "QuickSort implementation as a strategy"
  [items]
  (println "Sorting using QuickSort strategy")
  (if (< (count items) 2)
    items
    (let [pivot (first items)
          remaining (rest items)]
      (concat
        (quick-sort (filter #(< % pivot) remaining))
        [pivot]
        (quick-sort (filter #(>= % pivot) remaining))))))

(defn merge-sort
  "MergeSort implementation as a strategy"
  [items]
  (println "Sorting using MergeSort strategy")
  (if (< (count items) 2)
    items
    (let [split-pt (quot (count items) 2)
          part1 (take split-pt items)
          part2 (drop split-pt items)]
      (letfn [(merge* [res xs ys]
                (cond
                  (empty? xs) (concat res ys)
                  (empty? ys) (concat res xs)
                  :else (let [x (first xs)
                              y (first ys)]
                          (if (<= x y)
                            (merge* (conj res x) (rest xs) ys)
                            (merge* (conj res y) xs (rest ys))))))]
        (merge* [] (merge-sort part1) (merge-sort part2))))))

(defn bubble-sort
  "BubbleSort implementation as a strategy"
  [items]
  (println "Sorting using BubbleSort strategy")
  (let [item-count (count items)]
    (loop [items (vec items)
           swapped? true
           pass 0]
      (cond
        (not swapped?) items
        (>= pass (dec item-count)) (recur items false 0)
        :else
        (let [i pass
              current (get items i)
              next (get items (inc i))]
          (if (> current next)
            (recur (assoc items i next (inc i) current) true (inc pass))
            (recur items swapped? (inc pass))))))))

;; Context function accepting strategy as parameter
(defn sort-with-strategy
  "Sort items using the provided strategy function"
  [items strategy-fn]
  (strategy-fn items))

;; Strategy as a higher-order function
(defn create-sorting-strategy
  "Creates a sorting strategy with pre/post processing"
  [sort-fn pre-process post-process]
  (fn [items]
    (-> items
        pre-process
        sort-fn
        post-process)))

;; Factory Pattern in Clojure
;; Define behaviors as functions
(defn open-pdf [] (println "Opening PDF"))
(defn save-pdf [] (println "Saving PDF"))
(defn open-word [] (println "Opening Word doc"))
(defn save-word [] (println "Saving Word doc"))

;; Factory function returning a map of behaviors
(defn create-document [type]
  (case (str/lower-case type)
    "pdf" {:open open-pdf, :save save-pdf}
    "word" {:open open-word, :save save-word}
    (throw (IllegalArgumentException. "Unknown document type"))))

;; Observer Pattern in Clojure
;; Using atoms and watchers

(defn create-stock-market []
  (atom {}))

(defn display-watcher [key reference old-state new-state]
  (let [changed-stocks (filter (fn [[stock price]]
                                 (not= price (get old-state stock)))
                               new-state)]
    (doseq [[stock price] changed-stocks]
      (println "Display:" stock "price updated to" price))))

(defn alert-watcher [thresholds key reference old-state new-state]
  (let [changed-stocks (filter (fn [[stock price]]
                                 (and (contains? thresholds stock)
                                      (> price (get thresholds stock))
                                      (not= price (get old-state stock))))
                               new-state)]
    (doseq [[stock price] changed-stocks]
      (println "ALERT:" stock "exceeded threshold!"))))

;; Builder Pattern in Clojure
(defn create-email [& {:keys [from to subject body cc bcc headers]
                       :or {cc [], bcc [], headers {}}}]
  {:from from
   :to to
   :subject subject
   :body body
   :cc cc
   :bcc bcc
   :headers headers})

(defn add-cc [email cc-address]
  (update email :cc conj cc-address))

(defn add-header [email key value]
  (assoc-in email [:headers key] value))

;; Demo function to showcase all patterns
(defn demo []
  ;; Strategy Pattern Demo
  (println "\n--- Strategy Pattern Demo ---")
  (let [numbers [5 3 9 1 7 2 8 4 6]]
    (println "Original list:" numbers)

    ;; Using different sorting strategies
    (println "\nResults with different sorting strategies:")
    (println "QuickSort result:" (sort-with-strategy numbers quick-sort))
    (println "MergeSort result:" (sort-with-strategy numbers merge-sort))
    (println "BubbleSort result:" (sort-with-strategy numbers bubble-sort))

    ;; Using higher-order function to create enhanced strategies
    (println "\nEnhanced strategy with pre/post processing:")
    (let [enhanced-strategy (create-sorting-strategy
                              quick-sort
                              #(do (println "Pre-processing data...") %)
                              #(do (println "Post-processing results...") %))]
      (println (enhanced-strategy numbers))))

  ;; Factory Pattern Demo
  (println "\n--- Factory Pattern Demo ---")
  (let [pdf-doc (create-document "pdf")
        word-doc (create-document "word")]

    (println "Working with PDF:")
    ((:open pdf-doc))
    ((:save pdf-doc))

    (println "\nWorking with Word:")
    ((:open word-doc))
    ((:save word-doc)))

  ;; Observer Pattern Demo
  (println "\n--- Observer Pattern Demo ---")
  (let [stock-prices (create-stock-market)]

    ;; Add watchers
    (add-watch stock-prices :display display-watcher)
    (add-watch stock-prices :alert (partial alert-watcher {"AAPL" 150.0, "GOOG" 2000.0}))

    ;; Update stock prices (will trigger watchers)
    (println "Updating AAPL price to 145.75:")
    (swap! stock-prices assoc "AAPL" 145.75)

    (println "\nUpdating AAPL price to 155.75 (exceeds threshold):")
    (swap! stock-prices assoc "AAPL" 155.75)

    (println "\nUpdating GOOG price to 2100.50 (exceeds threshold):")
    (swap! stock-prices assoc "GOOG" 2100.50))

  ;; Builder Pattern Demo
  (println "\n--- Builder Pattern Demo ---")

  ;; Using named parameters approach
  (println "Creating email with named parameters:")
  (let [email (create-email
                :from "sender@example.com"
                :to "recipient@example.com"
                :subject "Meeting"
                :body "Let's meet tomorrow"
                :cc ["manager@example.com"]
                :headers {"Importance" "High"})]
    (println email))

  ;; Using threading macros approach
  (println "\nCreating email with threading macros:")
  (let [email (-> (create-email
                    :from "sender@example.com"
                    :to "recipient@example.com"
                    :subject "Meeting"
                    :body "Let's meet tomorrow")
                  (add-cc "manager@example.com")
                  (add-header "Importance" "High"))]
    (println email)))

;; Example REPL usage
(comment
  (demo)
  ;; Useful for testing individual patterns or strategies
  (quick-sort [5 3 9 1 7 2 8 4 6])
  (bubble-sort [5 3 9 1 7 2 8 4 6])
  (create-document "pdf"))