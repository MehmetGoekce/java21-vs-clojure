(ns clojure-examples.webscraper
  "Concurrent web scraper implemented in Clojure"
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.core.async :as async :refer [>! <! >!! <!! go go-loop chan
                                                  close! timeout alts! alts!!]]
            [org.httpkit.client :as http]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [cheshire.core :as cheshire]
            [clojure.java.io :as io])
  (:import [java.util.concurrent Executors TimeUnit]
           [java.util.regex Pattern]))

;; ===== Core Scraping Functions =====

(defn extract-title
  "Extract the title from HTML content"
  [html]
  (let [title-pattern #"<title>(.*?)</title>"
        matches (re-find title-pattern html)]
    (if matches
      (second matches)
      "No title found")))

(defn extract-text
  "Extract text content from HTML by removing tags"
  [html]
  (-> html
      (str/replace #"<script[^>]*>.*?</script>" "")
      (str/replace #"<style[^>]*>.*?</style>" "")
      (str/replace #"<!--.*?-->" "")
      (str/replace #"<[^>]*>" " ")
      (str/replace #"&nbsp;" " ")
      (str/replace #"\s+" " ")
      str/trim))

(defn count-words
  "Count words in extracted text"
  [text]
  (count (str/split text #"\s+")))

(defn extract-metadata
  "Extract metadata from HTML content"
  [html]
  (let [patterns {"description" #"<meta[^>]*name=[\"']description[\"'][^>]*content=[\"']([^\"']*)[\"']"
                  "keywords" #"<meta[^>]*name=[\"']keywords[\"'][^>]*content=[\"']([^\"']*)[\"']"
                  "og:title" #"<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']*)[\"']"
                  "og:description" #"<meta[^>]*property=[\"']og:description[\"'][^>]*content=[\"']([^\"']*)[\"']"}]
    (into {}
          (for [[key pattern] patterns]
            (let [matches (re-find pattern html)]
              [key (when matches (second matches))])))))

(defn count-keyword-frequencies
  "Count occurrences of keywords in the HTML content"
  [html keywords]
  (let [text (-> html
                 str/lower-case
                 extract-text)]
    (into {}
          (for [keyword keywords]
            [keyword (count (re-seq (re-pattern (str/lower-case keyword)) text))]))))

(defn extract-links
  "Extract all links from HTML content"
  [html]
  (let [link-pattern #"href=[\"'](https?://[^\"']+)[\"']"]
    (distinct (map second (re-seq link-pattern html)))))

(defn extract-images
  "Extract image URLs from HTML content"
  [html]
  (let [img-pattern #"<img[^>]*src=[\"']([^\"']*)[\"']"]
    (map second (re-seq img-pattern html))))

(defn scrape-url
  "Scrape a single URL and return structured data"
  [url keywords & {:keys [extract-images? follow-links? timeout-ms]
                   :or {extract-images? false
                        follow-links? false
                        timeout-ms 10000}}]
  (try
    (let [response @(http/get url {:timeout timeout-ms})
          html (:body response)]
      (if (= 200 (:status response))
        (let [text (extract-text html)
              result {:url url
                      :title (extract-title html)
                      :word-count (count-words text)
                      :keyword-frequency (count-keyword-frequencies html keywords)
                      :links (extract-links html)
                      :status (:status response)
                      :headers (select-keys (:headers response)
                                            ["content-type" "server" "last-modified"])}]
          ;; Add optional data based on parameters
          (cond-> result
                  extract-images? (assoc :images (extract-images html))
                  :always result))
        {:url url :error (str "HTTP Error: " (:status response))}))
    (catch Exception e
      {:url url :error (.getMessage e)})))

;; ===== Concurrent Scraping Strategies =====

;; Using Clojure's futures for simple concurrency
(defn scrape-urls-with-futures
  "Scrape multiple URLs concurrently using futures"
  [urls keywords & {:keys [extract-images? follow-links? timeout-ms]
                    :or {extract-images? false
                         follow-links? false
                         timeout-ms 10000}}]
  (let [futures (doall
                  (map #(future
                          (scrape-url % keywords
                                      :extract-images? extract-images?
                                      :follow-links? follow-links?
                                      :timeout-ms timeout-ms))
                       urls))
        results (doall (map deref futures))]
    results))

;; Using core.async for more flexible concurrency control
(defn scrape-urls-with-core-async
  "Scrape multiple URLs using core.async channels and workers"
  [urls keywords & {:keys [extract-images? follow-links? timeout-ms worker-count overall-timeout-ms]
                    :or {extract-images? false
                         follow-links? false
                         timeout-ms 10000
                         worker-count 5
                         overall-timeout-ms 30000}}]
  (let [input-chan (chan (count urls))
        output-chan (chan (count urls))
        timeout-chan (timeout overall-timeout-ms)]

    ;; Start worker processes
    (dotimes [_ worker-count]
      (go-loop []
        (if-let [url (<! input-chan)]
          (do
            (>! output-chan
                (scrape-url url keywords
                            :extract-images? extract-images?
                            :follow-links? follow-links?
                            :timeout-ms timeout-ms))
            (recur))
          :done)))

    ;; Feed URLs to workers
    (doseq [url urls]
      (>!! input-chan url))
    (close! input-chan)

    ;; Collect results with timeout
    (loop [results []
           remaining (count urls)]
      (if (zero? remaining)
        results
        (let [[result port] (alts!! [output-chan timeout-chan])]
          (if (= port timeout-chan)
            (conj results {:error "Overall scraping operation timed out"})
            (recur (conj results result) (dec remaining))))))))

;; Using Java's ExecutorService for thread pool management
(defn scrape-urls-with-executor
  "Scrape multiple URLs using Java ExecutorService"
  [urls keywords & {:keys [extract-images? follow-links? timeout-ms thread-count]
                    :or {extract-images? false
                         follow-links? false
                         timeout-ms 10000
                         thread-count 10}}]
  (let [executor (Executors/newFixedThreadPool thread-count)
        tasks (map (fn [url]
                     (fn []
                       (scrape-url url keywords
                                   :extract-images? extract-images?
                                   :follow-links? follow-links?
                                   :timeout-ms timeout-ms)))
                   urls)
        futures (.invokeAll executor tasks)
        results (map (fn [future]
                       (try
                         (.get future)
                         (catch Exception e
                           {:error (.getMessage e)})))
                     futures)]
    (.shutdown executor)
    (.awaitTermination executor 1 TimeUnit/MINUTES)
    results))

;; ===== Advanced Features =====

(defn follow-links-recursively
  "Recursively follow links up to specified depth"
  [start-url keywords max-depth & {:keys [max-urls-per-level timeout-ms]
                                   :or {max-urls-per-level 5
                                        timeout-ms 10000}}]
  (let [visited (atom #{start-url})
        results (atom [])]

    (letfn [(visit-url [url depth]
              (when (and (< depth max-depth)
                         (not (@visited url)))
                (swap! visited conj url)
                (let [result (scrape-url url keywords :timeout-ms timeout-ms)]
                  (swap! results conj (assoc result :depth depth))

                  ;; Follow links to next level with limits
                  (when (< depth (dec max-depth))
                    (let [links (->> (:links result)
                                     (remove @visited)
                                     (take max-urls-per-level))]
                      (doseq [link links]
                        (visit-url link (inc depth))))))))]

      ;; Start the recursive process
      (visit-url start-url 0)

      {:results @results
       :visited-count (count @visited)
       :stats {:by-depth (frequencies (map :depth @results))}})))

(defn create-sitemap
  "Create a sitemap from the scraped URLs"
  [results]
  (let [url-data (filter #(not (:error %)) results)
        site-structure (reduce
                         (fn [acc result]
                           (assoc acc (:url result)
                                      {:title (:title result)
                                       :links (count (:links result))
                                       :outbound-links (:links result)}))
                         {}
                         url-data)

        ;; Create adjacency map
        adjacency-map (reduce
                        (fn [adj-map result]
                          (let [from-url (:url result)]
                            (reduce
                              (fn [m to-url]
                                (update-in m [from-url] (fnil conj #{}) to-url))
                              adj-map
                              (:links result))))
                        {}
                        url-data)]

    {:nodes (count site-structure)
     :structure site-structure
     :adjacency adjacency-map}))

(defn analyze-content
  "Perform text analysis on the scraped content"
  [results]
  (let [successful-results (remove :error results)
        combined-keywords (reduce
                            (fn [acc result]
                              (merge-with + acc (:keyword-frequency result)))
                            {}
                            successful-results)

        avg-word-count (if (seq successful-results)
                         (/ (reduce + (map :word-count successful-results))
                            (count successful-results))
                         0)]

    {:total-pages (count successful-results)
     :failed-pages (count (filter :error results))
     :avg-word-count avg-word-count
     :keyword-frequency combined-keywords}))

;; ===== Utility Functions =====

(defn format-result
  "Format scraping result for display"
  [{:keys [url title word-count keyword-frequency links error]}]
  (if error
    (format "URL: %s\nERROR: %s\n" url error)
    (format "URL: %s\nTitle: %s\nWord Count: %d\nKeywords: %s\nLinks: %d\n"
            url title word-count keyword-frequency (count links))))

(defn save-results-to-file
  "Save scraping results to a file"
  [results filename & {:keys [format] :or {format :edn}}]
  (let [formatter (case format
                    :edn pr-str
                    :json json/write-str
                    pr-str)
        output-data (formatter results)]
    (spit filename output-data)))

(defn load-results-from-file
  "Load scraping results from a file"
  [filename & {:keys [format] :or {format :edn}}]
  (let [parser (case format
                 :edn edn/read-string
                 :json #(json/read-str % :key-fn keyword)
                 edn/read-string)]
    (-> filename slurp parser)))

;; ===== Rate Limiting and Politeness =====

(defn create-rate-limited-scraper
  "Create a rate-limited scraper function to be polite to web servers"
  [requests-per-minute]
  (let [interval-ms (/ (* 60 1000) requests-per-minute)
        last-request (atom (- (System/currentTimeMillis) interval-ms))]
    (fn [url keywords & options]
      (let [now (System/currentTimeMillis)
            time-since-last (- now @last-request)
            sleep-time (max 0 (- interval-ms time-since-last))]
        ;; Sleep if needed to respect rate limit
        (when (pos? sleep-time)
          (Thread/sleep sleep-time))
        ;; Update last request time and perform scrape
        (reset! last-request (System/currentTimeMillis))
        (apply scrape-url url keywords options)))))

;; ===== Progressive Enhancement Features =====

(defn detect-page-technology
  "Detect technologies used on a webpage from its content"
  [html]
  (let [tech-patterns {"WordPress" #"wp-content|wp-includes"
                       "React" #"react|reactjs"
                       "Angular" #"ng-|angular"
                       "Vue" #"Vue\.js|vue"
                       "jQuery" #"jquery"
                       "Bootstrap" #"bootstrap"
                       "Tailwind" #"tailwindcss|tailwind"
                       "Google Analytics" #"gtag|google-analytics|googletagmanager"
                       "Cloudflare" #"cloudflare"
                       "Shopify" #"shopify"
                       "Wix" #"wix\.com"
                       "Squarespace" #"squarespace"}]
    (into {}
          (for [[tech pattern] tech-patterns]
            [tech (boolean (re-find pattern html))]))))

(defn extract-schema-metadata
  "Extract schema.org metadata from HTML"
  [html]
  (let [schema-pattern #"<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>"
        matches (re-seq schema-pattern html)]
    (for [match matches]
      (try
        (json/read-str (second match) :key-fn keyword)
        (catch Exception _
          nil)))))

(defn enhanced-scrape
  "Enhanced version of scrape with more metadata extraction"
  [url keywords]
  (try
    (let [response @(http/get url {:timeout 15000})
          html (:body response)]
      (if (= 200 (:status response))
        (let [text (extract-text html)
              metadata (extract-metadata html)
              technologies (detect-page-technology html)
              schema-data (extract-schema-metadata html)
              result {:url url
                      :title (extract-title html)
                      :word-count (count-words text)
                      :keyword-frequency (count-keyword-frequencies html keywords)
                      :links (extract-links html)
                      :images (extract-images html)
                      :meta-tags metadata
                      :technologies technologies
                      :schema-org-data schema-data
                      :status (:status response)
                      :content-type (get-in response [:headers "content-type"])}]
          result)
        {:url url :error (str "HTTP Error: " (:status response))}))
    (catch Exception e
      {:url url :error (.getMessage e)})))

;; ===== Demo Functions =====

(defn scrape-demo-urls
  "Scrape a set of demo URLs with concurrency"
  []
  (let [urls ["https://example.com"
              "https://opensource.org"
              "https://www.wikipedia.org"]
        keywords ["open" "source" "free" "software" "web" "information"]

        ;; Scrape with different concurrency models and time them
        start-time-futures (System/currentTimeMillis)
        future-results (scrape-urls-with-futures urls keywords)
        future-time (- (System/currentTimeMillis) start-time-futures)

        start-time-async (System/currentTimeMillis)
        async-results (scrape-urls-with-core-async urls keywords)
        async-time (- (System/currentTimeMillis) start-time-async)

        start-time-executor (System/currentTimeMillis)
        executor-results (scrape-urls-with-executor urls keywords)
        executor-time (- (System/currentTimeMillis) start-time-executor)]

    {:results {:futures future-results
               :async async-results
               :executor executor-results}
     :timing {:futures future-time
              :async async-time
              :executor executor-time}
     :comparison (analyze-content future-results)}))

(defn run-enhanced-demo
  "Run the enhanced scraper demo"
  []
  (let [url "https://example.com"
        keywords ["example" "domain" "internet" "protocol" "web"]]
    (enhanced-scrape url keywords)))

(defn run-scraper-demonstrations
  "Run all web scraper demonstrations"
  []
  (let [demo-results (scrape-demo-urls)
        enhanced-demo (run-enhanced-demo)]

    ;; Create advanced demonstrations too
    (-> demo-results
        (assoc :enhanced enhanced-demo)
        (update :advanced
                (fn [_]
                  {:sitemap (create-sitemap (get-in demo-results [:results :futures]))
                   :content-analysis (analyze-content (get-in demo-results [:results :futures]))})))))

;; For REPL experimentation
(comment
  (run-scraper-demonstrations)
  (scrape-url "https://example.com" ["example" "domain"])
  (scrape-urls-with-futures ["https://example.com" "https://clojure.org"] ["clojure" "example"])
  (scrape-urls-with-core-async ["https://example.com" "https://opensource.org"] ["open" "source"])
  (enhanced-scrape "https://example.com" ["example" "domain"])
  (follow-links-recursively "https://example.com" ["example"] 2))

(defn -main []
  )