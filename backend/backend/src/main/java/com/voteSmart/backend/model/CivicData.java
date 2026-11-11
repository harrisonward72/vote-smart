package com.voteSmart.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CivicData {
    private String normalizedAddress;
    private List<Representative> representatives;
    private List<Election> elections;
    private ElectionInfo upcomingElection;
}
