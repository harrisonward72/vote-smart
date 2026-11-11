package com.voteSmart.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class BallotRace {
    private String office;
    private String level;
    private List<BallotCandidate> candidates;
}
