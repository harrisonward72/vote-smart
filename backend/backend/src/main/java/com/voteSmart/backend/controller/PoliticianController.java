package com.voteSmart.backend.controller;

import com.voteSmart.backend.service.AiSummaryService;
import com.voteSmart.backend.service.CapitolTraceService;
import com.voteSmart.backend.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/politician")
public class PoliticianController {

    private final AiSummaryService aiSummaryService;
    private final CapitolTraceService capitolTraceService;
    private final NewsService newsService;

    public PoliticianController(AiSummaryService aiSummaryService, CapitolTraceService capitolTraceService, NewsService newsService) {
        this.aiSummaryService = aiSummaryService;
        this.capitolTraceService = capitolTraceService;
        this.newsService = newsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary (
        @RequestParam String name,
        @RequestParam String role,
        @RequestParam String party,
        @RequestParam String jurisdiction){
        try {
            Map<String, Object> summary = aiSummaryService.generateSummary(name, role, party, jurisdiction);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.out.println("Summary error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/congressional-data")
    public ResponseEntity<?> getCongressionalData(@RequestParam String name) {
        try {
            Map<String, Object> data = capitolTraceService.getMemberProfile(name);
            if (data == null) {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "message", "No congressional profile found for this member"
                ));
            }
            data.put("found", true);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.out.println("Capitol Trace error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/news")
    public ResponseEntity<?> getNews(@RequestParam String name,
                                     @RequestParam String role,
                                     @RequestParam String jurisdiction) {
        try {
            List<Map<String, String>> articles = newsService.getNews(name, role, jurisdiction);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            System.out.println("News error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/challenger-summary")
    public ResponseEntity<?> getChallengerSummary (@RequestParam String name,
                                                   @RequestParam String party,
                                                   @RequestParam String office) {
        try {
            Map<String, Object> summary = aiSummaryService.generateChallengerSummary(name, party, office);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.out.println("Challenger summary error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/challenger-news")
    public ResponseEntity<?> getChallengerNews (@RequestParam String name, @RequestParam String office) {
        try {
            List<Map<String, String>> articles =
                    newsService.getNews(name, office, "United States");
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            System.out.println("Challenger news error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
