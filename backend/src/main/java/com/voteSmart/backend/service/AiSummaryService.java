package com.voteSmart.backend.service;

import com.voteSmart.backend.model.PoliticianCache;
import com.voteSmart.backend.repository.PoliticianCacheRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiSummaryService {

    @Value("${anthropic.api.key}")
    private String anthropicKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PoliticianCacheRepository cacheRepository;

    //how long before we refresh the cache
    private static final int CACHE_DAYS = 7;

    public AiSummaryService(PoliticianCacheRepository cacheRepository){
        this.cacheRepository = cacheRepository;
    }

    private String buildCacheKey(String name, String role) {
        return (name + "_" + role).toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
    }

    private void saveToCache(String cacheKey, Map<String, Object> summary, Optional<PoliticianCache> existing) {
        try {
            String json = objectMapper.writeValueAsString(summary);
            LocalDateTime now = LocalDateTime.now();

            PoliticianCache entry = existing.orElse(new PoliticianCache());
            entry.setCacheKey(cacheKey);
            entry.setSummaryJson(json);
            entry.setGeneratedAt(now);
            entry.setExpiresAt(now.plusDays(CACHE_DAYS));

            cacheRepository.save(entry);
            System.out.println("Saved to cache: " + cacheKey);
        } catch (Exception e){
            System.out.println("Cache save failed: " + e.getMessage()); //cache save failure shouldn't break the request
        }
    }

    public Map<String, Object> generateSummary(String name, String role,
                                               String party, String jurisdiction) throws Exception {
        String cacheKey = buildCacheKey(name, role);
        System.out.println("Cache key: " + cacheKey);

        //check cache first
        Optional<PoliticianCache> cached = cacheRepository.findByCacheKey(cacheKey);

        if (cached.isPresent()) {
            PoliticianCache entry = cached.get();
            if (entry.getExpiresAt().isAfter(LocalDateTime.now())) {
                System.out.println("Cache HIT for: " + cacheKey);
                return objectMapper.readValue(entry.getSummaryJson(), Map.class);
            } else {
                System.out.println("Cache expired for: " + cacheKey);
            }
        } else {
            System.out.println("Cache missed for: " + cacheKey);
        }

        //cache missed or expired
        System.out.println("Calling claude for: " + name);
        Map<String, Object> summary = callClaude(name, role, party, jurisdiction);

        //save to cache
        saveToCache(cacheKey, summary, cached);

        return summary;
    }

    public Map<String, Object> generateChallengerSummary (String name, String party, String office) throws Exception {
        String cacheKey = "challenger_" + name.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        System.out.println("Challenger cache key: " + cacheKey);

        Optional<PoliticianCache> cached = cacheRepository.findByCacheKey(cacheKey);
        if(cached.isPresent() && cached.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            System.out.println("Challenger cache hit: " + cacheKey);
            return objectMapper.readValue(cached.get().getSummaryJson(), Map.class);
        }

        System.out.println("Challenger cache miss: " + cacheKey);
        Map<String, Object> summary = callClaudeForChallenger(name, party, office);
        saveToCache(cacheKey, summary, cached);
        return summary;
    }

    private Map<String, Object> callClaudeForChallenger(String name, String party,
                                                        String office) throws Exception {
        String prompt = """
        Research %s, a %s candidate running for %s.
        
        CRITICAL: Return ONLY a valid JSON object. No markdown, no explanations, no asterisks.
        If you cannot find information about this specific person, still return the JSON
        structure with "Not available" for unknown fields. Never return text outside the JSON.
        
        {
          "background": {
            "age": "number or Not available",
            "birthplace": "city, state or Not available",
            "education": ["degree - institution - year or Not available"],
            "career": ["job title - employer - years or Not available"],
            "military_service": "branch - years - rank or None",
            "family": "Not available",
            "criminal_history": "None on record",
            "political_experience": "Not available"
          },
          "campaign": {
            "summary": "summary of their known platform or Not available",
            "economy": "Not available",
            "healthcare": "Not available",
            "energy": "Not available",
            "gun_policy": "Not available",
            "abortion": "Not available",
            "immigration": "Not available",
            "foreign_policy": "Not available",
            "key_issues": ["Not available"]
          },
          "funding": {
            "total_raised": "Not available",
            "top_donors": ["Not available"],
            "funding_summary": "Not available"
          },
          "notable": {
            "endorsements": [],
            "controversies": []
          },
          "social_media": {
            "twitter": "Not available",
            "facebook": "Not available",
            "instagram": "Not available",
            "website": "Not available"
          }
        }
        """.formatted(name, party, office);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-6");
        requestBody.put("max_tokens", 2000);
        requestBody.put("messages", messages);

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
        JsonNode root = objectMapper.readTree(response.getBody());
        String content = root.path("content").path(0).path("text").asText();

        System.out.println("Claude raw content: " + content);

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');

        if (start == -1 || end == -1 || start >= end) {
            System.out.println("No valid JSON in response");
            throw new Exception("Could not parse Claude response as JSON");
        }

        String jsonOnly = content.substring(start, end + 1);

        try {
            return objectMapper.readValue(jsonOnly, Map.class);
        } catch (Exception e) {
            System.out.println("Failed to parse summary JSON: " + e.getMessage());
            throw new Exception("Failed to parse Claude response");
        }
    }



    private Map<String, Object> callClaude(String name, String role, String party, String jurisdiction) throws Exception {
        String prompt = buildPrompt(name, role, party, jurisdiction);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-6");
        requestBody.put("max_tokens", 2000);
        requestBody.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.anthropic.com/v1/messages", HttpMethod.POST, entity, String.class
        );
        JsonNode root = objectMapper.readTree(response.getBody());
        String content = root.path("content")
                .path(0)
                .path("text")
                .asText();

        String cleaned = content
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        return objectMapper.readValue(cleaned, Map.class);
    }

    private String buildPrompt(String name, String role,
                               String party, String jurisdiction) {
        return """
            Research %s, currently serving as %s (%s) representing %s.

            Return ONLY a valid JSON object with exactly these fields.
            Do not include any text before or after the JSON.
            If information is not publicly available, use "Not available".
            Be factual and objective. Do not editorialize.

            {
              "background": {
                "age": "number or Not available",
                "birthplace": "city, state or Not available",
                "education": ["degree - institution - year"],
                "career_before_office": ["job title - employer - years"],
                "military_service": "branch - years - rank or None",
                "family": "brief family description or Not available",
                "criminal_history": "description or None on record"
              },
              "time_in_office": "X years (year - present) or Not available",
              "platform": {
                "summary": "2-3 sentence summary of their platform",
                "economy": "their stated position",
                "healthcare": "their stated position",
                "energy": "their stated position",
                "gun_policy": "their stated position",
                "abortion": "their stated position",
                "immigration": "their stated position",
                "foreign_policy": "their stated position"
              },
              "notable": {
                "achievements": ["achievement 1", "achievement 2"],
                "controversies": ["controversy 1 with source if known"]
              },
              "social_media": {
                "twitter": "@handle or Not available",
                "facebook": "url or Not available",
                "instagram": "@handle or Not available",
                "website": "url or Not available"
              }
            }
            """.formatted(name, role, party, jurisdiction);
    }


    private Map<String, Object> buildEmptyProfile(String name, String party) {
        Map<String, Object> profile = new HashMap<>();

        Map<String, Object> background = new HashMap<>();
        background.put("age", "Not available");
        background.put("birthplace", "Not available");
        background.put("education", List.of("Not available"));
        background.put("career", List.of("Not available"));
        background.put("military_service", "Not available");
        background.put("family", "Not available");
        background.put("criminal_history", "None on record");
        background.put("political_experience", "Not available");
        profile.put("background", background);

        Map<String, Object> campaign = new HashMap<>();
        campaign.put("summary", "No campaign information available at this time.");
        campaign.put("economy", "Not available");
        campaign.put("healthcare", "Not available");
        campaign.put("energy", "Not available");
        campaign.put("gun_policy", "Not available");
        campaign.put("abortion", "Not available");
        campaign.put("immigration", "Not available");
        campaign.put("foreign_policy", "Not available");
        campaign.put("key_issues", List.of());
        profile.put("campaign", campaign);

        Map<String, Object> funding = new HashMap<>();
        funding.put("total_raised", "Not available");
        funding.put("top_donors", List.of());
        funding.put("funding_summary", "Not available");
        profile.put("funding", funding);

        Map<String, Object> notable = new HashMap<>();
        notable.put("endorsements", List.of());
        notable.put("controversies", List.of());
        profile.put("notable", notable);

        Map<String, Object> social = new HashMap<>();
        social.put("twitter", "Not available");
        social.put("facebook", "Not available");
        social.put("instagram", "Not available");
        social.put("website", "Not available");
        profile.put("social_media", social);

        return profile;
    }

}
