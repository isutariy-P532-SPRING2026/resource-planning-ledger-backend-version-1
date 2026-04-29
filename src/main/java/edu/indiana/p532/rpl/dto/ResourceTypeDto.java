package edu.indiana.p532.rpl.dto;

import edu.indiana.p532.rpl.domain.ResourceKind;

public record ResourceTypeDto(
        String name,
        ResourceKind kind,
        String unitOfMeasure
) {}
