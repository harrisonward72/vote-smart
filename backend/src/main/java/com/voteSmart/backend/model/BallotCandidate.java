package com.voteSmart.backend.model;

import lombok.Data;

@Data
public class BallotCandidate {
    private String name;
    private String party;
    private String incumbentStatus;
    private String website;

}
