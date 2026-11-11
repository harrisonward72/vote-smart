package com.voteSmart.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class Election {
    private String id;
    private String name;
    private String electionDay;
}
