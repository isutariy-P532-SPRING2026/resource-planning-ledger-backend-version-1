package edu.indiana.p532.rpl.dto;

import java.util.List;

public record ProtocolDto(
        String name,
        String description,
        List<ProtocolStepDto> steps
) {}
