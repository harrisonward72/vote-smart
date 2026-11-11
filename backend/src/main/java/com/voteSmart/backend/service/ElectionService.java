package com.voteSmart.backend.service;

import com.voteSmart.backend.model.BallotCandidate;
import com.voteSmart.backend.model.BallotRace;
import com.voteSmart.backend.model.ElectionInfo;
import com.voteSmart.backend.model.PollingLocation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElectionService {

    public ElectionInfo getUpcomingElection(String address) {
        //REPLACE WITH REAL API CAL WHEN ABLE TO PAY FOR ONE
        //FOR NOW RETURN DUMMY DATA
        ElectionInfo election = new ElectionInfo();
        election.setElectionName("2026 Midterm General Election");
        election.setElectionDate("November 3rd, 2026");
        election.setRegistrationDeadline("October 13, 2026");

        //Polling Location based on address
        PollingLocation polling = new PollingLocation();
        polling.setName("King George County Government Center");
        polling.setAddress("10459 Courthouse Dr, King George, VA 22485");
        polling.setHours("6:00 AM - 7:00 PM");
        election.setPollingLocation(polling);

        //Early voting location
        PollingLocation earlyVoting = new PollingLocation();
        earlyVoting.setName("King George County Registrar Office");
        earlyVoting.setAddress("10087 Kings Highway, King George, VA 22485");
        earlyVoting.setHours("Mon-Sat 8:00 AM - 5:00 PM (Sept 19 - Nov 1");
        election.setEarlyVotingLocation(earlyVoting);

        //Ballot Races
        List<BallotRace> races = new ArrayList<>();

        //US Senate race
        BallotRace senateRace = new BallotRace();
        senateRace.setOffice("U.S. Senate - Virginia");
        senateRace.setLevel("Federal");
        List<BallotCandidate> senateCandidates = new ArrayList<>();
        senateCandidates.add(createCandidate("Tim Kaine", "Democratic", "Incumbent", "https://timkaine.com"));
        senateCandidates.add(createCandidate("Mike Chase", "Republican", "Challenger", ""));
        senateRace.setCandidates(senateCandidates);
        races.add(senateRace);

        // US House race
        BallotRace houseRace = new BallotRace();
        houseRace.setOffice("U.S. House - Virginia 1st District");
        houseRace.setLevel("Federal");
        List<BallotCandidate> houseCandidates = new ArrayList<>();
        houseCandidates.add(createCandidate("Rob Wittman", "Republican", "Incumbent", "https://wittman.house.gov"));
        houseCandidates.add(createCandidate("Leslye Gilchrist", "Democratic", "Challenger", ""));
        houseRace.setCandidates(houseCandidates);
        races.add(houseRace);


        // State Senate race
        BallotRace stateSenateRace = new BallotRace();
        stateSenateRace.setOffice("Virginia State Senate - District 28");
        stateSenateRace.setLevel("State");
        List<BallotCandidate> stateSenateCandidates = new ArrayList<>();
        stateSenateCandidates.add(createCandidate("Bryce Reeves", "Republican", "Incumbent", ""));
        stateSenateCandidates.add(createCandidate("Michelle Maldonado", "Democratic", "Challenger", ""));
        stateSenateRace.setCandidates(stateSenateCandidates);
        races.add(stateSenateRace);

        election.setRaces(races);
        return election;
    }

    private BallotCandidate createCandidate(String name, String party, String status, String website) {
        BallotCandidate candidate = new BallotCandidate();
        candidate.setName(name);
        candidate.setParty(party);
        candidate.setIncumbentStatus(status);
        candidate.setWebsite(website);
        return candidate;
    }
}
