package edu.indiana.p532.rpl.dto;

import edu.indiana.p532.rpl.domain.AllocationKind;

import java.math.BigDecimal;

public record ResourceAllocationRequest(
        Long resourceTypeId,
        BigDecimal quantity,
        AllocationKind kind,
        String assetId,
        String timePeriod
) {}
