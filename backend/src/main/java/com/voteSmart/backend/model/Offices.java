package com.voteSmart.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Offices {
    private String name;
    private String phone;
    private String address;
    private String classification;
}
