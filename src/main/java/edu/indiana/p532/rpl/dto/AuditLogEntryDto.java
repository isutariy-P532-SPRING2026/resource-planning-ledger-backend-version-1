package edu.indiana.p532.rpl.dto;

public record AuditLogEntryDto(
        Long id,
        String event,
        Long accountId,
        Long entryId,
        Long actionId,
        String timestamp,
        String details
) {}
