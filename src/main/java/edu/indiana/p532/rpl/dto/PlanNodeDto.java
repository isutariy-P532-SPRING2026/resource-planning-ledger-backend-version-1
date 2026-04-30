package edu.indiana.p532.rpl.dto;

import java.util.List;

public record PlanNodeDto(
        Long id,
        String name,
        String type,                    // "PLAN" or "ACTION"
        String status,
        List<String> legalTransitions,  // populated for ACTION nodes; empty for PLAN nodes
        List<PlanNodeDto> children,
        String targetStartDate,         // populated for PLAN nodes
        String dependsOn                // populated for ACTION nodes from protocol step
) {}
