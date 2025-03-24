package com.example.webscraper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demonstrates a concurrent web scraper using Java 21 virtual threads
 * and modern HTTP client
 */
public class WebScraper {
    // Using Java 21's HttpClient which supports virtual threads
    private final HttpClient client;

    public WebScraper() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public record ScrapingResult(
            String url,
            String title,
            int wordCount,
            Map<String, Integer> keywordFrequency,
            List<String> links
    ) {
        @Override
        public String toString() {
            return String.format(
                    "URL: %s\nTitle: %s\nWord Count: %d\nKeywords: %s\nLinks: %d\n",
                    url, title, wordCount, keywordFrequency, links.size()
            );
        }
    }

    public List<ScrapingResult> scrapeUrls(List<String> urls, List<String> keywords) {
        Map<String, ScrapingResult> results = new ConcurrentHashMap<>();

        // Using Java 21's Virtual Threads for concurrency
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (String url : urls) {
                futures.add(executor.submit(() -> {
                    try  {
                        ScrapingResult result = scrapeUrl(url, keywords);
                        results.put(url, result);
                    } catch (Exception e) {
                        System.err.println(STR."Error scraping: \{url}: \{e.getMessage()}");
                    }
                }));
            }

            for (Future<?> future: futures) {
                future.get();
            }
        } catch (Exception e) {
            System.err.println(STR."Error during concurrent scraping: \{e.getMessage()}");
        }
        return new ArrayList<>(results.values());
    }

    private ScrapingResult scrapeUrl(String url, List<String> keywords) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        String html = response.body();

        // Extract title
        String title = extractTitle(html);

        // Count words
        int wordCount = countWords(html);

        // Count keyword frequencies
        Map<String, Integer> keywordFrequency = countKeywordFrequencies(html, keywords);

        // Extract links
        List<String> links = extractLinks(html);

        return new ScrapingResult(url, title, wordCount, keywordFrequency, links);
    }

    private String extractTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.*?)</title>");
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : "No title found";
    }

    private int countWords(String html) {
        // Remove HTML tags and count words
        String text = html.replaceAll("<[^>]*>", " ");
        String[] words = text.split("\\s+");
        return words.length;
    }

    private Map<String, Integer> countKeywordFrequencies(String html, List<String> keywords) {
        Map<String, Integer> frequencies = new HashMap<>();
        String text = html.toLowerCase().replaceAll("<[^>]*>", " ");

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int count = 0;
            int index = 0;

            while ((index = text.indexOf(lowerKeyword, index)) != -1) {
                count++;
                index += lowerKeyword.length();
            }

            frequencies.put(keyword, count);
        }

        return frequencies;
    }

    private List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("href=[\"'](http[^\"']+)[\"']");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            links.add(matcher.group(1));
        }

        return links;
    }

    // Wrapper method for demo use with limited URLs
    public void scrapeDemoUrls(List<String> urls, List<String> keywords) {
        System.out.println("Starting web scraping demo with Java 21 virtual threads...");

        long startTime = System.currentTimeMillis();
        List<ScrapingResult> results = scrapeUrls(urls, keywords);
        long endTime = System.currentTimeMillis();

        System.out.println(STR."Scraping completed in \{endTime - startTime}ms");
        System.out.println("Scraping Results:");
        results.forEach(System.out::println);
    }

    // Enhanced scraping with metadata extraction
    public record EnhancedScrapingResult(
            ScrapingResult baseResult,
            Map<String, String> metadata,
            List<String> images,
            String contentSummary
    ) {}

    public EnhancedScrapingResult enhancedScrape(String url, List<String> keywords) {
        try {
            // Get base scraping result
            ScrapingResult baseResult = scrapeUrl(url, keywords);

            // Extract metadata
            Map<String, String> metadata = extractMetadata(baseResult.title, url);

            // Extract images
            List<String> images = extractImageUrls(baseResult.url);

            // Generate content summary
            String contentSummary = generateSummary(baseResult);

            return new EnhancedScrapingResult(baseResult, metadata, images, contentSummary);

        } catch (Exception e) {
            System.err.println(STR."Error during enhanced scraping: \{e.getMessage()}");
            return null;
        }
    }

    private Map<String, String> extractMetadata(String title, String url) {
        // In a real implementation, this would extract OpenGraph and other metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("url", url);
        metadata.put("scrapedAt", java.time.LocalDateTime.now().toString());
        return metadata;
    }

    private List<String> extractImageUrls(String url) {
        // This would extract image URLs from the page
        // For this demo, we'll return an empty list
        return new ArrayList<>();
    }

    private String generateSummary(ScrapingResult result) {
        // In a real implementation, this would generate a summary of the content
        // Here we just return a placeholder
        return STR."Content summary for \{result.url()} with \{result.wordCount()} words and \{result.links().size()} links.";
    }

    // Demo usage
    public static void main(String[] args) {
        WebScraper scraper = new WebScraper();

        List<String> urls = List.of(
                "https://example.com",
                "https://opensource.org",
                "https://www.wikipedia.org"
        );

        List<String> keywords = List.of("open", "source", "free", "software", "web");

        scraper.scrapeDemoUrls(urls, keywords);
    }
}
