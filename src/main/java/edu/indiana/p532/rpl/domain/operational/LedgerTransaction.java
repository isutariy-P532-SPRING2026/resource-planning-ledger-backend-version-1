package edu.indiana.p532.rpl.domain.operational;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ledger_transactions")
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // originating action id for audit linkage
    @Column(name = "originating_action_id")
    private Long originatingActionId;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entry> entries = new ArrayList<>();

    protected LedgerTransaction() {}

    public LedgerTransaction(String description, Long originatingActionId) {
        this.description = description;
        this.originatingActionId = originatingActionId;
        this.createdAt = Instant.now();
    }

    public void addEntry(Entry entry) {
        entry.setTransaction(this);
        entries.add(entry);
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getOriginatingActionId() { return originatingActionId; }
    public List<Entry> getEntries() { return entries; }
}
