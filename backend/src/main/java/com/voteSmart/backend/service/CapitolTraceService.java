package com.voteSmart.backend.service;

import com.voteSmart.backend.model.PoliticianCache;
import com.voteSmart.backend.repository.PoliticianCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CapitolTraceService {

    @Value("${capitoltrace.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PoliticianCacheRepository cacheRepository;

    private static final String BASE_URL = "https://api.capitoltrace.com/v1";
    private static final int CACHE_DAYS = 7;

    public CapitolTraceService(PoliticianCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public Map<String, Object> getMemberProfile(String fullName) throws Exception {
        String cacheKey = "ct_profile_" + fullName.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        // Check cache
        Optional<PoliticianCache> cached = cacheRepository.findByCacheKey(cacheKey);
        if (cached.isPresent() && cached.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            System.out.println("Capitol Trace cache HIT: " + cacheKey);
            return objectMapper.readValue(cached.get().getSummaryJson(), Map.class);
        }

        System.out.println("Capitol Trace cache MISS for: " + fullName);

        // Find the bioguide ID
        String bioguideId = findBioguideId(fullName);
        if (bioguideId == null) {
            System.out.println("No Capitol Trace match for: " + fullName);
            return null;
        }

        System.out.println("Found bioguideId: " + bioguideId);

        // Get full member profile
        Map<String, Object> profile = fetchMemberProfile(bioguideId);

        // Save to cache
        saveToCache(cacheKey, profile, cached);
        return profile;
    }

    private String findBioguideId(String fullName) throws Exception {
        // Open States gives "First Last"
        // Capitol Trace stores "Last, First"
        String[] parts = fullName.trim().split("\\s+", 2);
        String lastFirst = parts.length == 2
                ? parts[1] + ", " + parts[0]
                : fullName;

        System.out.println("Searching Capitol Trace for: " + lastFirst);

        // Paginate through all members to find match
        for (int page = 1; page <= 11; page++) {
            String url = BASE_URL + "/members?page=" + page;
            JsonNode response = makeGetRequest(url);
            JsonNode data = response.path("data");

            for (JsonNode member : data) {
                String memberName = member.path("name").asText();
                if (namesMatch(memberName, lastFirst, parts)) {
                    return member.path("bioguideId").asText();
                }
            }

            int totalPages = response.path("pagination")
                    .path("total_pages").asInt(1);
            if (page >= totalPages) break;

            // Small delay to be respectful of rate limits
            Thread.sleep(150);
        }

        return null;
    }

    private boolean namesMatch(String capitolTraceName,
                               String lastFirst, String[] parts) {
        // Exact match on "Last, First"
        if (capitolTraceName.equalsIgnoreCase(lastFirst)) return true;

        // Partial match — both first and last name present
        String ctLower = capitolTraceName.toLowerCase();
        if (parts.length == 2) {
            return ctLower.contains(parts[0].toLowerCase()) &&
                    ctLower.contains(parts[1].toLowerCase());
        }

        return false;
    }

    private Map<String, Object> fetchMemberProfile(String bioguideId) throws Exception {
        String url = BASE_URL + "/members/" + bioguideId;
        JsonNode response = makeGetRequest(url);
        JsonNode data = response.path("data");

        Map<String, Object> profile = new HashMap<>();
        profile.put("bioguideId", bioguideId);
        profile.put("imageUrl", data.path("imageUrl").asText(""));
        profile.put("officialWebsite", data.path("officialWebsiteUrl").asText(""));
        profile.put("birthYear", data.path("birthYear").asText(""));
        profile.put("sponsoredCount", data.path("sponsoredLegislation")
                .path("count").asInt(0));
        profile.put("cosponsoredCount", data.path("cosponsoredLegislation")
                .path("count").asInt(0));

        // Parse terms
        JsonNode terms = data.path("terms");
        List<Map<String, Object>> termList = new ArrayList<>();
        int earliestYear = 9999;

        for (JsonNode term : terms) {
            Map<String, Object> t = new HashMap<>();
            t.put("chamber", term.path("chamber").asText());
            t.put("startYear", term.path("startYear").asInt());
            t.put("endYear", term.path("endYear").asInt(0));
            termList.add(t);

            int startYear = term.path("startYear").asInt(9999);
            if (startYear < earliestYear) earliestYear = startYear;
        }

        profile.put("terms", termList);

        if (earliestYear < 9999) {
            profile.put("firstElected", earliestYear);
            profile.put("yearsInOffice", 2026 - earliestYear);
        }

        // GovTrack URL is predictable from bioguide ID
        profile.put("govtrackUrl",
                "https://www.govtrack.us/congress/members/" +
                        bioguideId.toLowerCase());

        // Congress.gov URL
        profile.put("congressUrl",
                "https://www.congress.gov/member/x/" + bioguideId);

        return profile;
    }

    private JsonNode makeGetRequest(String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
        );

        return objectMapper.readTree(response.getBody());
    }

    private void saveToCache(String cacheKey, Map<String, Object> data,
                             Optional<PoliticianCache> existing) {
        try {
            String json = objectMapper.writeValueAsString(data);
            LocalDateTime now = LocalDateTime.now();

            PoliticianCache entry = existing.orElse(new PoliticianCache());
            entry.setCacheKey(cacheKey);
            entry.setSummaryJson(json);
            entry.setGeneratedAt(now);
            entry.setExpiresAt(now.plusDays(CACHE_DAYS));
            cacheRepository.save(entry);
            System.out.println("Capitol Trace saved to cache: " + cacheKey);
        } catch (Exception e) {
            System.out.println("Capitol Trace cache save failed: " + e.getMessage());
        }
    }
}