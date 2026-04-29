package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.operational.AuditLogEntry;
import edu.indiana.p532.rpl.dto.AuditLogEntryDto;
import edu.indiana.p532.rpl.repository.AuditLogEntryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final AuditLogEntryRepository auditLogEntryRepository;

    public AuditLogController(AuditLogEntryRepository auditLogEntryRepository) {
        this.auditLogEntryRepository = auditLogEntryRepository;
    }

    @GetMapping
    public List<AuditLogEntryDto> list() {
        return auditLogEntryRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream().map(this::toDto).toList();
    }

    private AuditLogEntryDto toDto(AuditLogEntry e) {
        return new AuditLogEntryDto(
                e.getId(), e.getEvent(), e.getAccountId(), e.getEntryId(),
                e.getActionId(), e.getTimestamp().toString(), e.getDetails()
        );
    }
}
