package edu.indiana.p532.rpl.dto;

import java.math.BigDecimal;

public record AccountDto(
        Long id,
        String name,
        String kind,
        BigDecimal balance,
        boolean belowZero
) {}
