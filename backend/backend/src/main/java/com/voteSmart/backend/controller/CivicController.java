package com.voteSmart.backend.controller;

import com.voteSmart.backend.model.CivicData;
import com.voteSmart.backend.model.ElectionInfo;
import com.voteSmart.backend.service.CivicService;
import com.voteSmart.backend.service.ElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/civic")
public class CivicController {

    private final CivicService civicService;
    private final ElectionService electionService;

    public CivicController(CivicService civicService, ElectionService electionService) {
        this.civicService = civicService;
        this.electionService = electionService;
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String address) {
        try {
            CivicData data = civicService.lookupAddress(address);

            // Always add fresh election data — it's hardcoded so no API cost
            if (data.getUpcomingElection() == null) {
                data.setUpcomingElection(
                        electionService.getUpcomingElection(address));
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.out.println("Controller error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
