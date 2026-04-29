package edu.indiana.p532.rpl.dto;

import java.time.Instant;

public record ImplementActionRequest(
        String actualParty,
        String actualLocation,
        Instant actualStart
) {}
