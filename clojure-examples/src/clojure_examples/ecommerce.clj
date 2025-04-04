(ns clojure-examples.ecommerce
  "E-commerce order processing system in Clojure demonstrating functional design"
  (:require [clojure.core.async :refer [chan go >! <! <!! >!! close! go-loop timeout alts!]]
            [java-time :as time]))

;; Domain Models - Pure data using maps
(defn create-customer
  "Create a customer record"
  [id name email & {:keys [address phone]}]
  {:id id
   :name name
   :email email
   :address address
   :phone phone
   :created-at (time/local-date-time)})

(defn create-product
  "Create a product record"
  [id name category price stock-level & {:keys [description tags]}]
  {:id id
   :name name
   :category category
   :price price
   :stock-level stock-level
   :description description
   :tags tags})

(defn create-order-item
  "Create an order item"
  [product-id quantity price]
  {:product-id product-id
   :quantity quantity
   :price price})

(defn get-item-total
  "Calculate total for an order item"
  [item]
  (* (:quantity item) (:price item)))

(defn create-order
  "Create a new order"
  [id customer-id items]
  {:id id
   :customer-id customer-id
   :items items
   :created-at (time/local-date-time)
   :status :created
   :payment-info nil})

(defn get-order-total
  "Calculate total for an order"
  [order]
  (reduce + (map get-item-total (:items order))))

;; Repository functions - Simulate database operations with atoms
(defn create-repositories
  "Create in-memory repositories with atoms"
  []
  {:customers (atom {})
   :products (atom {})
   :orders (atom {})})

(defn setup-demo-data
  "Set up sample data for demonstration"
  [repos]
  (let [{:keys [customers products]} repos]
    ;; Add customers
    (swap! customers assoc "cust1"
           (create-customer "cust1" "John Doe" "john@example.com"))
    (swap! customers assoc "cust2"
           (create-customer "cust2" "Jane Smith" "jane@example.com"))

    ;; Add products
    (swap! products assoc "prod1"
           (create-product "prod1" "Laptop" "Electronics" 999.99 10))
    (swap! products assoc "prod2"
           (create-product "prod2" "Headphones" "Electronics" 149.99 20))
    (swap! products assoc "prod3"
           (create-product "prod3" "T-shirt" "Clothing" 24.99 50))

    repos))

;; Service functions - Business logic
(defn check-availability
  "Check if a product is available in requested quantity"
  [product quantity]
  (>= (:stock-level product) quantity))

(defn process-payment
  "Process a payment (simulated)"
  [customer-id amount payment-method]
  (let [transaction-id (str (java.util.UUID/randomUUID))]
    {:transaction-id transaction-id
     :method payment-method
     :amount amount
     :processed-at (time/local-date-time)}))

;; Order Service - Orchestration functions
(defn create-new-order
  "Create a new order with validation"
  [repos customer-id product-quantities]
  (let [{:keys [customers products orders]} repos]
    ;; Verify customer exists
    (if-let [customer (get @customers customer-id)]
      (let [product-ids (keys product-quantities)
            found-products (select-keys @products product-ids)]

        ;; Check all products exist and are available
        (if (= (count found-products) (count product-ids))
          (let [inventory-checks
                (for [[product-id quantity] product-quantities
                      :let [product (get found-products product-id)]]
                  {:product-id product-id
                   :requested quantity
                   :available? (check-availability product quantity)})
                unavailable (filter (comp not :available?) inventory-checks)]

            (if (empty? unavailable)
              ;; All products available, create order
              (let [items (for [[product-id quantity] product-quantities
                                :let [product (get found-products product-id)]]
                            (create-order-item product-id quantity (:price product)))
                    order-id (str (java.util.UUID/randomUUID))
                    order (create-order order-id customer-id items)]

                ;; Save order
                (swap! orders assoc order-id order)

                ;; Return created order
                order)

              ;; Some products unavailable
              {:error "Some products are not available"
               :unavailable-products (map :product-id unavailable)}))

          ;; Some products not found
          {:error "Some products not found"
           :missing-products (vec (remove (set (keys found-products)) product-ids))}))

      ;; Customer not found
      {:error "Customer not found"})))

(defn process-payment-for-order
  "Process payment for an order"
  [repos order-id payment-method]
  (let [{:keys [products orders]} repos]
    (if-let [order (get @orders order-id)]
      (if (= (:status order) :created)
        ;; Process payment
        (let [order-total (get-order-total order)
              payment-info (process-payment (:customer-id order)
                                            order-total
                                            payment-method)
              updated-order (-> order
                                (assoc :payment-info payment-info)
                                (assoc :status :paid))]

          ;; Save updated order
          (swap! orders assoc order-id updated-order)

          ;; Update product inventory
          (doseq [item (:items order)]
            (swap! products update-in
                   [(:product-id item) :stock-level]
                   - (:quantity item)))

          updated-order)

        ;; Order not in created state
        {:error "Order is not in created state"})

      ;; Order not found
      {:error "Order not found"})))

(defn demo
  "Demonstrate e-commerce workflow"
  ([] (demo (-> (create-repositories)
                setup-demo-data)))
  ([repos]
   (println "=== E-commerce Workflow Demonstration ===")

   ;; Create an order
   (println "\n1. Creating Order")
   (let [order (create-new-order repos "cust1"
                                 {"prod1" 1  ; 1 Laptop
                                  "prod2" 2  ; 2 Headphones
                                  })]
     (println "Order created:" (dissoc order :items))
     (println "Order Total: CHF" (get-order-total order))

     ;; Process payment
     (println "\n2. Processing Payment")
     (let [paid-order (process-payment-for-order repos (:id order) "credit_card")]
       (println "Order paid. Status:" (:status paid-order))
       (println "Payment Details:" (:payment-info paid-order)))

     ;; Get current product inventory
     (println "\n3. Updated Product Inventory")
     (doseq [[id product] @(:products repos)]
       (println (format "%s: %s (%d in stock)"
                        id
                        (:name product)
                        (:stock-level product))))

     ;; Return the repositories for further use if needed
     repos)))

;; REPL usage and exploration
(comment
  (demo)
  (let [repos (create-repositories)]
    (setup-demo-data repos)
    (demo repos)))