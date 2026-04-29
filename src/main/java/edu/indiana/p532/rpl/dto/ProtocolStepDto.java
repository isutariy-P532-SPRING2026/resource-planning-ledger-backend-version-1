package edu.indiana.p532.rpl.dto;

public record ProtocolStepDto(
        String name,
        Long subProtocolId,
        String dependsOn
) {}
