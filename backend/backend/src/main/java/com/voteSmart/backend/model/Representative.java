package com.voteSmart.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Representative {
    private String name;
    private String party;
    private String role;
    private String phone;
    private String website;
    private String email;
    private String image;
    private String birthDate;
    private String jurisdiction;
    private String openStatesUrl;
    private List<Offices> offices;

}
