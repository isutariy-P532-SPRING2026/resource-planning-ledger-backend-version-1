package edu.indiana.p532.rpl.domain.operational;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "entries")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // positive = deposit/credit, negative = withdrawal/debit
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "charged_at", nullable = false)
    private Instant chargedAt;

    @Column(name = "booked_at", nullable = false)
    private Instant bookedAt;

    @Column(name = "description")
    private String description;

    protected Entry() {}

    public Entry(LedgerTransaction transaction, Account account, BigDecimal amount,
                 Instant chargedAt, Instant bookedAt, String description) {
        this.transaction = transaction;
        this.account = account;
        this.amount = amount;
        this.chargedAt = chargedAt;
        this.bookedAt = bookedAt;
        this.description = description;
    }

    public Long getId() { return id; }
    public LedgerTransaction getTransaction() { return transaction; }
    public void setTransaction(LedgerTransaction transaction) { this.transaction = transaction; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getChargedAt() { return chargedAt; }
    public Instant getBookedAt() { return bookedAt; }
    public String getDescription() { return description; }
}
