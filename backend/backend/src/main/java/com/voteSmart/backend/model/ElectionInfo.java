package com.voteSmart.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class ElectionInfo {
    private String electionName;
    private String electionDate;
    private String registrationDeadline;
    private PollingLocation pollingLocation;
    private PollingLocation earlyVotingLocation;
    private List<BallotRace> races;
}
