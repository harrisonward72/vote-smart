package com.voteSmart.backend.service;

import com.voteSmart.backend.model.PoliticianCache;
import com.voteSmart.backend.repository.PoliticianCacheRepository;
import jakarta.websocket.OnClose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NewsService {

    @Value("${anthropic.api.key}")
    private String anthropicKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PoliticianCacheRepository cacheRepository;

    private static final int CACHE_HOURS = 168;

    public NewsService(PoliticianCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public List<Map<String, String>> getNews (String name, String role, String jurisdiction) throws Exception {
        String cacheKey = "news_" + name.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");

        //check cache
        Optional<PoliticianCache> cached = cacheRepository.findByCacheKey(cacheKey);
        if(cached.isPresent() && cached.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            System.out.println("News cache HIT: " + cacheKey);
            return objectMapper.readValue(cached.get().getSummaryJson(), List.class);
        }

        System.out.println("Cache miss: " + cacheKey);
        List<Map<String, String>> articles = fetchNewsFromClaude(name, role, jurisdiction);

        //save to cache
        try {
            String json = objectMapper.writeValueAsString(articles);
            LocalDateTime now = LocalDateTime.now();
            PoliticianCache entry = cached.orElse(new PoliticianCache());
            entry.setCacheKey(cacheKey);
            entry.setSummaryJson(json);
            entry.setGeneratedAt(now);
            entry.setExpiresAt(now.plusHours(CACHE_HOURS));
            cacheRepository.save(entry);
            System.out.println("News cached: " + cacheKey);
        } catch (Exception e) {
            System.out.println("News cache failed to save: " + e.getMessage());
        }
        return articles;
    }

    private List<Map<String, String>> fetchNewsFromClaude(String name, String role, String jurisdiction) throws Exception {
        String prompt = """
                Search for the 6 most recent news articles about %s, who serves as %s representing %s.
                
                            Return ONLY a valid JSON array with no text before or after it.
                            Each item in the array must have exactly these fields:
                            - "headline": the article headline
                            - "source": the news outlet name (e.g. CNN, Fox News, Reuters, AP, Politico)
                            - "url": the direct URL to the article
                            - "summary": one sentence summarizing the article
                            - "lean": political lean of the source - must be exactly one of: "left", "center", "right"
                
                            Example format:
                            [
                              {
                                "headline": "Senator votes against budget bill",
                                "source": "Reuters",
                                "url": "https://reuters.com/article/...",
                                "summary": "Senator opposed the bill citing fiscal concerns.",
                                "lean": "center"
                              }
                            ]
                
                            Only include articles with real, working URLs.
                            Try to include a mix of sources across the political spectrum when available.
                """.formatted(name, role, jurisdiction);

        //Build message
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message);

        //build web search tool
        Map<String, Object> webSearchTool = new HashMap<>();
        webSearchTool.put("type", "web_search_20250305");
        webSearchTool.put("name", "web_search");
        webSearchTool.put("max_uses", 3);

        //build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-6");
        requestBody.put("max_tokens", 2000);
        requestBody.put("messages", messages);
        requestBody.put("tools", List.of(webSearchTool));

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.anthropic.com/v1/messages",
                HttpMethod.POST,
                entity,
                String.class
        );

        System.out.println("News claude status: " + response.getStatusCode());

        //parse response --- find the text block in the content array
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentArray = root.path("content");

        String textContent = "";
        for (JsonNode block : contentArray) {
            if ("text".equals(block.path("type").asText())) {
                textContent = block.path("text").asText();
                break;
            }
        }

        System.out.println("News raw content: " + textContent);

        int start = textContent.indexOf('[');
        int end = textContent.lastIndexOf(']');

        if (start == -1 || end == -1 || start >= end) {
            System.out.println("No valid JSON array in news response, returning empty list");
            return new ArrayList<>();
        }

        String jsonOnly = textContent.substring(start, end + 1);

        try {
            return objectMapper.readValue(jsonOnly, List.class);
        } catch (Exception e) {
            System.out.println("Failed to parse news JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }


}
