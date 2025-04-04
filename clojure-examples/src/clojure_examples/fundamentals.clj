(ns clojure-examples.fundamentals
  (:require [clojure.string :as str]))

;; Pure Functions: Basic Transformations
(defn add-tax
  "Calculate total amount including tax"
  [amount tax-rate]
  (+ amount (* amount tax-rate)))

(defn capitalize-words
  "Capitalize each word in a string"
  [s]
  (str/join " " (map str/capitalize (str/split s #"\s+"))))

(defn calculate-order-total
  "Calculate the total for an order including tax"
  [order]
  (let [subtotal (reduce + (map :price (:items order)))
        tax (* subtotal (:tax-rate order))
        total (+ subtotal tax)]
    (assoc order :subtotal subtotal :tax tax :total total)))

;; First-Class Functions: Utility Functions
(defn double-value
  "Double the input value"
  [x]
  (* 2 x))

(defn square
  "Square the input value"
  [x]
  (* x x))

(defn apply-twice
  "Apply a function twice to a value"
  [f x]
  (f (f x)))

(defn create-multiplier
  "Create a function that multiplies by a given factor"
  [factor]
  (fn [x] (* x factor)))

;; Demonstration Functions
(defn pure-functions-demo []
  (println "\n--- Pure Functions Demonstration ---")

  ;; Tax calculation examples
  (println "Adding 10% tax to $100:" (add-tax 100 0.10))
  (println "Adding 8% tax to $50:" (add-tax 50 0.08))

  ;; String transformation example
  (println "Capitalizing 'hello world':" (capitalize-words "hello world"))

  ;; Order processing example
  (let [order {:customer "John"
               :items [{:name "Book" :price 15.00}
                       {:name "Coffee" :price 3.50}]
               :tax-rate 0.08}
        processed (calculate-order-total order)]
    (println "\nProcessed order:")
    (println "Subtotal: $" (:subtotal processed))
    (println "Tax: $" (:tax processed))
    (println "Total: $" (:total processed))))

(defn immutability-demo []
  (println "\n--- Immutability Demonstration ---")

  ;; Immutable vector operations
  (let [numbers [1 2 3 4]
        more-numbers (conj numbers 5)
        first-removed (subvec more-numbers 1)]
    (println "Original vector:" numbers)
    (println "After adding 5:" more-numbers)
    (println "After removing first element:" first-removed)
    (println "Original vector remains unchanged:" numbers))

  ;; Immutable map operations
  (let [person {:name "Alice" :age 30}
        updated (assoc person :age 31)
        with-email (assoc updated :email "alice@example.com")
        without-age (dissoc with-email :age)]
    (println "\nOriginal map:" person)
    (println "Updated age:" updated)
    (println "Added email:" with-email)
    (println "Removed age:" without-age)
    (println "Original map remains unchanged:" person))

  ;; Demonstrating structural sharing
  (let [original (vec (range 1000))
        modified (assoc original 500 :modified)]
    (println "\nStructural sharing demonstration:")
    (println "Original and modified vectors have same object at index 0?:"
             (identical? (get original 0) (get modified 0)))))

(defn first-class-functions-demo []
  (println "\n--- First-Class Functions Demonstration ---")

  ;; Functions as values
  (println "Double 5:" (double-value 5))
  (println "Square 4:" (square 4))

  ;; Higher-order function example
  (println "Applying double twice to 3:" (apply-twice double-value 3))
  (println "Applying square twice to 2:" (apply-twice square 2))

  ;; Function factory example
  (let [triple (create-multiplier 3)
        quadruple (create-multiplier 4)]
    (println "Triple 7:" (triple 7))
    (println "Quadruple 5:" (quadruple 5)))

  ;; Collection operations with higher-order functions
  (let [numbers (range 1 11)] ; [1 2 3 4 5 6 7 8 9 10]
    (println "\nFilter even numbers:" (filter even? numbers))
    (println "Map square to numbers:" (map square numbers))
    (println "Reduce with addition:" (reduce + numbers))

    ;; Composition of operations
    (println "Chain operations (numbers > 3, squared, summed):")
    (println (->> numbers
                  (filter #(> % 3))
                  (map square)
                  (reduce +)))))

(defn function-composition-demo []
  (println "\n--- Function Composition Demonstration ---")

  ;; Basic function composition with comp
  (let [inc-and-double (comp (partial * 2) inc)
        double-and-inc (comp inc (partial * 2))]
    (println "inc-and-double applied to 3:" (inc-and-double 3)) ; (3+1)*2 = 8
    (println "double-and-inc applied to 3:" (double-and-inc 3))) ; (3*2)+1 = 7

  ;; Thread-first macro for sequential transformations
  (let [process-data (fn [data]
                       (-> data
                           (assoc :processed true)
                           (update :count inc)
                           (update :value * 2)))]
    (println "\nThread-first (->) processing result:"
             (process-data {:processed false :count 0 :value 10})))

  ;; Thread-last macro for collection operations
  (let [result (->> (range 1 11)
                    (filter odd?)
                    (map square)
                    (reduce +))]
    (println "Thread-last (->>) result - sum of squares of odd numbers 1-10:" result))

  ;; Partial application
  (let [add (fn [x y] (+ x y))
        add5 (partial add 5)
        add10 (partial add 10)]
    (println "\nPartial application:")
    (println "add5 applied to 3:" (add5 3))
    (println "add10 applied to 7:" (add10 7))))

(defn closures-demo []
  (println "\n--- Closures Demonstration ---")

  ;; Counter factory using closure
  (let [make-counter (fn []
                       (let [count-atom (atom 0)]
                         (fn []
                           (swap! count-atom inc))))
        counter1 (make-counter)
        counter2 (make-counter)]
    (println "Counter 1 first call:" (counter1))
    (println "Counter 1 second call:" (counter1))
    (println "Counter 1 third call:" (counter1))
    (println "Counter 2 first call:" (counter2))
    (println "Counter 2 second call:" (counter2)))

  ;; Closure that remembers configuration
  (let [create-greeter (fn [greeting]
                         (fn [name]
                           (str greeting ", " name "!")))
        hello-greeter (create-greeter "Hello")
        howdy-greeter (create-greeter "Howdy")]
    (println (hello-greeter "Alice"))
    (println (howdy-greeter "Bob"))))

(defn data-transformation-demo []
  (println "\n--- Data Transformation Demonstration ---")

  (let [orders [{:id 1 :customer "Alice" :items [{:product "Book" :price 15.99}]}
                {:id 2 :customer "Bob" :items [{:product "Laptop" :price 999.99}
                                               {:product "Mouse" :price 25.99}]}
                {:id 3 :customer "Charlie" :items [{:product "Coffee" :price 4.99}
                                                   {:product "Mug" :price 12.99}
                                                   {:product "Notebook" :price 8.99}]}
                {:id 4 :customer "Alice" :items [{:product "Headphones" :price 149.99}
                                                 {:product "Phone case" :price 19.99}]}]
        add-totals (fn [orders]
                     (map (fn [order]
                            (let [total (reduce + (map :price (:items order)))]
                              (assoc order :total total)))
                          orders))]

    ;; Display orders with total prices
    (println "Orders with totals:")
    (doseq [order (add-totals orders)]
      (println (format "ID: %d - Customer: %s - Total: $%.2f"
                       (:id order) (:customer order) (:total order))))

    ;; Group orders by customer
    (let [by-customer (group-by :customer orders)]
      (println "\nOrders grouped by customer:")
      (doseq [[customer orders] by-customer]
        (println (format "%s has %d orders" customer (count orders)))))

    ;; Calculate total spent per customer
    (let [orders-with-totals (add-totals orders)
          by-customer (group-by :customer orders-with-totals)
          customer-totals (map (fn [[customer orders]]
                                 [customer (reduce + (map :total orders))])
                               by-customer)]
      (println "\nTotal spent per customer:")
      (doseq [[customer total] customer-totals]
        (println (format "%s: $%.2f" customer total))))))

(defn lazy-sequence-demo []
  (println "\n--- Lazy Sequence Demonstration ---")

  ;; Creating an infinite sequence of fibonacci numbers
  (let [fibonacci-seq (fn []
                        ((fn fib [a b]
                           (lazy-seq
                             (cons a (fib b (+ a b)))))
                         0 1))]
    (println "First 10 Fibonacci numbers:")
    (println (take 10 (fibonacci-seq))))

  ;; Creating a lazy sequence of prime numbers
  (let [prime? (fn [n]
                 (and (> n 1)
                      (not-any? #(zero? (mod n %))
                                (range 2 (inc (Math/sqrt n))))))

        primes (fn []
                 (filter prime? (iterate inc 2)))]
    (println "First 10 prime numbers:")
    (println (take 10 (primes))))

  ;; Demonstrating lazy evaluation with infinite sequence
  (println "Sum of first 100 even numbers:"
           (reduce + (take 100 (filter even? (range))))))

(defn demo
  "Run all fundamental functional programming demonstrations"
  []
  (println "=== Functional Programming Fundamentals in Clojure ===")
  (pure-functions-demo)
  (immutability-demo)
  (first-class-functions-demo)
  (function-composition-demo)
  (closures-demo)
  (data-transformation-demo)
  (lazy-sequence-demo))

;; REPL experimentation support
(comment
  (demo)
  (pure-functions-demo)
  (immutability-demo)
  (first-class-functions-demo)
  (function-composition-demo)
  (closures-demo)
  (data-transformation-demo)
  (lazy-sequence-demo))