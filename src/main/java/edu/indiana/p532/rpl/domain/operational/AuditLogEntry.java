package edu.indiana.p532.rpl.domain.operational;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "action_id")
    private Long actionId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    protected AuditLogEntry() {}

    public AuditLogEntry(String event, Long accountId, Long entryId, Long actionId, String details) {
        this.event = event;
        this.accountId = accountId;
        this.entryId = entryId;
        this.actionId = actionId;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public Long getId() { return id; }
    public String getEvent() { return event; }
    public Long getAccountId() { return accountId; }
    public Long getEntryId() { return entryId; }
    public Long getActionId() { return actionId; }
    public Instant getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
}
