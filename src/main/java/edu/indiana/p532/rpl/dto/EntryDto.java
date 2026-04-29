package edu.indiana.p532.rpl.dto;

import java.math.BigDecimal;

public record EntryDto(
        Long id,
        Long accountId,
        String accountName,
        BigDecimal amount,
        String chargedAt,
        String bookedAt,
        Long originatingActionId,
        String description
) {}
