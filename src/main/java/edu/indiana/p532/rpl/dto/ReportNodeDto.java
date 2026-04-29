package edu.indiana.p532.rpl.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReportNodeDto(
        Long id,
        String name,
        String type,
        String status,
        int depth,
        /** Total allocated quantity per resource-type name (F10). Empty map for PLAN nodes. */
        Map<String, BigDecimal> allocations
) {}
