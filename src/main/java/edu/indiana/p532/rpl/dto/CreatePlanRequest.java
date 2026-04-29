package edu.indiana.p532.rpl.dto;

import java.time.LocalDate;

public record CreatePlanRequest(
        String name,
        Long sourceProtocolId,   // null = scratch plan
        LocalDate targetStartDate
) {}
