package com.voteSmart.backend.service;

import com.voteSmart.backend.model.*;
import com.voteSmart.backend.repository.PoliticianCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CivicService {

    @Value("${openstates.api.key}")
    private String openStatesKey;

    @Value("${google.geocoding.api.key}")
    private String geocodingKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PoliticianCacheRepository cacheRepository;

    // Cache civic data for 24 hours — reps don't change often
    private static final int CACHE_HOURS = 24;

    public CivicService(PoliticianCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public CivicData lookupAddress(String address) throws Exception {
        // Build a cache key from the normalized address
        String cacheKey = "civic_" + address.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        System.out.println("Civic cache key: " + cacheKey);

        // Check cache first
        Optional<com.voteSmart.backend.model.PoliticianCache> cached =
                cacheRepository.findByCacheKey(cacheKey);

        if (cached.isPresent() &&
                cached.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            System.out.println("Civic cache HIT for: " + address);
            return objectMapper.readValue(
                    cached.get().getSummaryJson(), CivicData.class);
        }

        System.out.println("Civic cache MISS for: " + address);

        // Cache miss — do the full lookup
        String state = extractState(address);
        System.out.println("Extracted state: " + state);

        double[] coords = geocodeAddress(address, state);
        List<Representative> reps = lookupReps(coords[0], coords[1]);

        CivicData civicData = new CivicData();
        civicData.setNormalizedAddress(address);
        civicData.setRepresentatives(reps);
        civicData.setElections(new ArrayList<>());

        // Save to cache
        saveCivicCache(cacheKey, civicData, cached);

        return civicData;
    }

    private void saveCivicCache(String cacheKey, CivicData data,
                                Optional<com.voteSmart.backend.model.PoliticianCache> existing) {
        try {
            String json = objectMapper.writeValueAsString(data);
            LocalDateTime now = LocalDateTime.now();

            com.voteSmart.backend.model.PoliticianCache entry =
                    existing.orElse(new com.voteSmart.backend.model.PoliticianCache());
            entry.setCacheKey(cacheKey);
            entry.setSummaryJson(json);
            entry.setGeneratedAt(now);
            entry.setExpiresAt(now.plusHours(CACHE_HOURS));
            cacheRepository.save(entry);
            System.out.println("Civic data cached: " + cacheKey);
        } catch (Exception e) {
            System.out.println("Civic cache save failed: " + e.getMessage());
        }
    }

    private String extractState(String address) {
        try {
            String[] parts = address.split(",");
            String lastPart = parts[parts.length - 1].trim();
            String[] stateParts = lastPart.split(" ");
            return stateParts[0].trim().toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

    private double[] geocodeAddress(String address, String state) throws Exception {
        String addressWithoutZip = address
                .replaceAll("\\s+\\d{5}(-\\d{4})?$", "").trim();
        System.out.println("Address without zip: " + addressWithoutZip);

        String url = UriComponentsBuilder
                .fromUriString("https://geocoding.geo.census.gov/geocoder/locations/onelineaddress")
                .queryParam("address", addressWithoutZip)
                .queryParam("benchmark", "Public_AR_Current")
                .queryParam("format", "json")
                .toUriString();

        System.out.println("Census Geocoding URL: " + url);
        String response = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(response);
        JsonNode matches = root.path("result").path("addressMatches");

        if (matches.isEmpty()) {
            throw new Exception("Address not found. Please check your address and try again.");
        }

        JsonNode coords = matches.path(0).path("coordinates");
        double lat = coords.path("y").asDouble();
        double lng = coords.path("x").asDouble();

        System.out.println("Geocoded to: " + lat + " " + lng);

        if (lat < 24.0 || lat > 50.0 || lng < -125.0 || lng > -66.0) {
            throw new Exception("Address could not be located within the United States.");
        }

        return new double[]{lat, lng};
    }

    private List<Representative> lookupReps(double lat, double lng) throws Exception {
        String url = UriComponentsBuilder
                .fromUriString("https://v3.openstates.org/people.geo")
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("include", "offices")
                .toUriString();

        System.out.println("Open States URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", openStatesKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

        System.out.println("Open States response: " + response.getBody());

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode results = root.path("results");

        List<Representative> reps = new ArrayList<>();

        for (JsonNode person : results) {
            Representative rep = new Representative();
            rep.setName(person.path("name").asText());
            rep.setParty(person.path("party").asText());
            rep.setRole(person.path("current_role").path("title").asText());
            rep.setEmail(person.path("email").asText("N/A"));
            rep.setImage(person.path("image").asText(""));
            rep.setBirthDate(person.path("birth_date").asText(""));
            rep.setJurisdiction(person.path("jurisdiction").path("name").asText());
            rep.setOpenStatesUrl(person.path("openstates_url").asText(""));

            JsonNode links = person.path("links");
            rep.setWebsite(links.isArray() && links.size() > 0
                    ? links.path(0).path("url").asText("N/A")
                    : "N/A");

            JsonNode officesNode = person.path("offices");
            List<Offices> officeList = new ArrayList<>();
            if (officesNode.isArray()) {
                for (JsonNode o : officesNode) {
                    Offices office = new Offices();
                    office.setName(o.path("name").asText());
                    office.setPhone(o.path("voice").asText("N/A"));
                    office.setAddress(o.path("address").asText("N/A"));
                    office.setClassification(o.path("classification").asText());
                    officeList.add(office);
                }
            }
            rep.setOffices(officeList);
            rep.setPhone(!officeList.isEmpty()
                    ? officeList.get(0).getPhone()
                    : "N/A");

            reps.add(rep);
        }

        return reps;
    }
}
