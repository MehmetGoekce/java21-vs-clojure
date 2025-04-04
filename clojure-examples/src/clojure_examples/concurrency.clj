(ns clojure-examples.concurrency
  "Demonstrates Clojure's concurrency primitives and patterns"
  (:require [clojure.core.async :as async :refer [>! <! >!! <!! go go-loop chan
                                                  close! timeout alts! alts!!]]
            [clojure.string :as str]))

;; ===== Atoms =====
;; Used for independent synchronous, uncoordinated state management

(defn atom-examples
  "Demonstrates atom usage for shared state"
  []
  (let [counter (atom 0)
        increment #(swap! counter inc)
        get-count #(deref counter) ; or @counter

        ;; For demonstrating CAS operations
        attempt-cas #(compare-and-set! counter @counter (+ @counter 10))

        ;; Update with function
        update-with-fn #(swap! counter (fn [current] (+ current %)))

        ;; For demonstrating reset
        reset-counter #(reset! counter 0)]

    ;; Execute operations
    (increment)
    (increment)
    (let [current-1 (get-count)
          cas-result (attempt-cas)
          current-2 (get-count)]
      (update-with-fn 5)
      (let [current-3 (get-count)]
        (reset-counter)
        {:started-at 0
         :after-increments current-1
         :cas-successful? cas-result
         :after-cas current-2
         :after-update current-3
         :after-reset @counter}))))

;; Atom with validators and watches
(defn atom-validators-and-watches
  "Demonstrates validators and watches with atoms"
  []
  (let [;; Validator ensures account never goes below zero
        account (atom 100 :validator #(>= % 0))

        ;; Watch function to log account changes
        log-changes (atom [])

        _ (add-watch account :logger
                     (fn [key ref old-state new-state]
                       (swap! log-changes conj
                              {:old old-state :new new-state :change (- new-state old-state)})))

        deposit #(swap! account + %)
        withdraw #(try
                    (swap! account - %)
                    true
                    (catch Exception e
                      false))]

    ;; Perform operations
    (deposit 50)
    (withdraw 30)
    (withdraw 200) ; Should fail validation
    (withdraw 120) ; Should succeed

    {:final-balance @account
     :transaction-log @log-changes
     :success? (= @account 0)}))

;; ===== Refs and STM =====
;; Used for coordinated synchronous changes across multiple identities

(defn transfer-example
  "Demonstrates transaction using refs for bank transfer"
  []
  (let [account-a (ref 1000)
        account-b (ref 500)

        ;; Function to transfer between accounts
        transfer (fn [from to amount]
                   (dosync
                     (alter from - amount)
                     (alter to + amount)))

        ;; Records operations performed
        operations (atom [])]

    ;; Record initial state
    (swap! operations conj {:a @account-a :b @account-b :total (+ @account-a @account-b)})

    ;; Perform transfers
    (transfer account-a account-b 200)
    (swap! operations conj {:a @account-a :b @account-b :total (+ @account-a @account-b)})

    (transfer account-b account-a 50)
    (swap! operations conj {:a @account-a :b @account-b :total (+ @account-a @account-b)})

    ;; Try a transfer that would cause negative balance (won't work with ensure)
    (try
      (dosync
        (ensure account-a) ; Read and lock the ref
        (when (>= @account-a 2000)
          (alter account-a - 2000)
          (alter account-b + 2000)))
      (catch Exception e
        (swap! operations conj {:error "Transaction failed"})))

    (swap! operations conj {:a @account-a :b @account-b :total (+ @account-a @account-b)})

    {:operations @operations
     :final-state {:account-a @account-a
                   :account-b @account-b
                   :total-preserved? (= (+ @account-a @account-b) 1500)}}))

;; ===== Agents =====
;; Used for independent asynchronous actions

(defn agent-examples
  "Demonstrates agent usage for asynchronous updates"
  []
  (let [counter (agent 0)
        log (agent [])

        ;; Functions to send to agents
        increment #(send counter inc)
        log-event #(send log conj %)

        ;; Function that will throw an exception
        cause-error #(send counter (fn [_] (throw (Exception. "Simulated error"))))

        ;; Error handler for counter
        _ (set-error-handler! counter (fn [agt err]
                                        (send log conj {:agent agt :error (.getMessage err)})
                                        0)) ; Reset to 0 on error

        ;; Actions with different execution modes
        slow-action #(send counter (fn [n]
                                     (Thread/sleep 100)
                                     (inc n)))
        slow-action-off-thread #(send-off counter (fn [n]
                                                    (Thread/sleep 100)
                                                    (inc n)))]

    ;; Execute operations
    (increment)
    (log-event {:message "Counter incremented"})
    (slow-action)
    (log-event {:message "Slow action queued"})
    (cause-error)
    (log-event {:message "Error action queued"})
    (slow-action-off-thread)
    (log-event {:message "Off-thread action queued"})

    ;; Wait for all actions to complete
    (await counter log)

    {:counter-value @counter
     :log @log}))

;; ===== core.async =====
;; CSP-style concurrency with channels

(defn basic-channel-examples
  "Demonstrates basic core.async channel operations"
  []
  (let [results (atom [])

        ;; Create channels
        c1 (chan)
        c2 (chan)

        ;; Add to results in a thread-safe way
        add-result #(swap! results conj %)]

    ;; Start two go-blocks to demonstrate communication
    (go
      (>! c1 "Hello")
      (add-result {:sent "Hello on c1"}))

    (go
      (let [msg (<! c1)]
        (add-result {:received msg})
        (>! c2 (str msg " World"))))

    (go
      (let [msg (<! c2)]
        (add-result {:final msg})))

    ;; Allow time for processing
    (Thread/sleep 100)

    @results))

(defn worker-pool-example
  "Demonstrates a worker pool using core.async"
  []
  (let [results (atom [])

        ;; Create channels
        input-chan (chan 10)
        output-chan (chan 10)

        ;; Function for workers to process
        work-fn (fn [x]
                  (Thread/sleep (rand-int 50)) ; Simulate varying processing times
                  (* x x))

        ;; Start worker processes
        _ (dotimes [worker-id 3]
            (go-loop []
              (when-let [item (<! input-chan)]
                (>! output-chan
                    {:worker worker-id
                     :input item
                     :output (work-fn item)
                     :time (System/currentTimeMillis)})
                (recur))))

        ;; Send items to be processed
        _ (doseq [i (range 1 11)]
            (>!! input-chan i))

        ;; Close input channel to signal no more work
        _ (close! input-chan)

        ;; Collect results
        _ (dotimes [_ 10]
            (swap! results conj (<!! output-chan)))

        ;; Close output channel
        _ (close! output-chan)]

    {:input-range '(1 to 10)
     :results (sort-by :input @results)}))

(defn timeout-and-alts-example
  "Demonstrates timeout and alts! for selection between channels"
  []
  (let [results (atom [])
        add-result #(swap! results conj %)

        ;; Create channels for different operations
        fast-op-chan (chan)
        slow-op-chan (chan)
        timer (timeout 150) ; 150ms timeout

        ;; Simulate fast operation (completes in 50ms)
        _ (go
            (Thread/sleep 50)
            (>! fast-op-chan "Fast operation result")
            (add-result {:sent "Fast operation result"}))

        ;; Simulate slow operation (completes in 200ms)
        _ (go
            (Thread/sleep 200)
            (>! slow-op-chan "Slow operation result")
            (add-result {:sent "Slow operation result"}))

        ;; Use alts! to take first available result or timeout
        _ (go
            (let [[val port] (alts! [fast-op-chan slow-op-chan timer])]
              (cond
                (= port timer)
                (add-result {:result "Operation timed out"})

                (= port fast-op-chan)
                (add-result {:result (str "Got fast result: " val)})

                (= port slow-op-chan)
                (add-result {:result (str "Got slow result: " val)}))))

        ;; Wait for second operation beyond timeout
        _ (go
            (let [val (<! slow-op-chan)]
              (add-result {:late-result (str "Eventually got slow result: " val)})))]

    ;; Allow time for all operations to complete
    (Thread/sleep 250)

    @results))

(defn pipeline-processing-example
  "Demonstrates a data processing pipeline with core.async"
  []
  (let [;; Create channels for each stage
        input (chan 10)
        validated (chan 10)
        transformed (chan 10)
        output (chan 10)

        ;; Data validation stage
        _ (go-loop []
            (when-let [data (<! input)]
              (if (and (map? data) (:value data) (number? (:value data)))
                (do
                  (>! validated data)
                  (recur))
                (recur))))

        ;; Data transformation stage
        _ (go-loop []
            (when-let [data (<! validated)]
              (let [transformed-data (update data :value #(* % %))]
                (>! transformed transformed-data)
                (recur))))

        ;; Output enrichment stage
        _ (go-loop []
            (when-let [data (<! transformed)]
              (let [enriched-data (assoc data
                                    :processed-at (System/currentTimeMillis)
                                    :status "COMPLETED")]
                (>! output enriched-data)
                (recur))))

        ;; Collect results
        results (atom [])
        collector (go-loop []
                    (when-let [data (<! output)]
                      (swap! results conj data)
                      (recur)))]

    ;; Send test data through the pipeline
    (>!! input {:id 1 :value 5})
    (>!! input {:id 2 :value 10})
    (>!! input {:id 3 :value "invalid"}) ; Should be filtered out
    (>!! input {:id 4 :value 15})

    ;; Close input to signal end of data
    (close! input)

    ;; Wait for processing to complete
    (Thread/sleep 100)

    ;; Close all channels
    (close! validated)
    (close! transformed)
    (close! output)

    {:processed-count (count @results)
     :results @results}))

(defn promesa-like-example
  "Implements a Promise-like construct using core.async"
  []
  (let [create-promise (fn []
                         (let [c (chan 1)
                               deliver (fn [value]
                                         (go (>! c value)
                                             (close! c)))
                               then (fn [f]
                                      (let [c2 (chan 1)]
                                        (go
                                          (let [v (<! c)]
                                            (>! c2 (f v))
                                            (close! c2)))
                                        c2))]
                           {:chan c
                            :deliver deliver
                            :then then}))

        ;; Create some promises
        p1 (create-promise)
        p2 (create-promise)

        ;; Chain operations
        p3 ((:then p1) #(inc %))
        p4 ((:then p3) #(* % 2))

        ;; Deliver values
        _ ((:deliver p1) 5)
        _ ((:deliver p2) "Hello")

        ;; Results
        results (atom {})]

    ;; Collect results
    (go (swap! results assoc :p1 (<! (:chan p1))))
    (go (swap! results assoc :p2 (<! (:chan p2))))
    (go (swap! results assoc :p3 (<! (:chan p3))))
    (go (swap! results assoc :p4 (<! (:chan p4))))

    ;; Allow time for processing
    (Thread/sleep 100)

    @results))

(defn demo
  "Run all concurrency demonstrations"
  []
  {:atom-examples (atom-examples)
   :atom-validators-watches (atom-validators-and-watches)
   :ref-transaction-example (transfer-example)
   :agent-examples (agent-examples)
   :core-async {:basic-channels (basic-channel-examples)
                :worker-pool (worker-pool-example)
                :timeout-and-alts (timeout-and-alts-example)
                :pipeline-processing (pipeline-processing-example)
                :promise-like (promesa-like-example)}})

;; For REPL experimentation
(comment
  (demo)
  (atom-examples)
  (atom-validators-and-watches)
  (transfer-example)
  (agent-examples)
  (basic-channel-examples)
  (worker-pool-example)
  (timeout-and-alts-example)
  (pipeline-processing-example)
  (promesa-like-example))


