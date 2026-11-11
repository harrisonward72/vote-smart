package com.voteSmart.backend.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PoliticianSummary {
    private String name;
    private String role;
    private String party;
    private String jurisdiction;
    private Map<String, Object> background;
    private String TimeInOffice;
    private Map<String, String> platform;
    private Map<String, List<String>>notable;
    private Map<String, String> socialMedia;
    private Long generatedAt;
}
