package edu.indiana.p532.rpl.domain.operational;

import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "suspensions")
public class Suspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false)
    private ProposedAction proposedAction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    protected Suspension() {}

    public Suspension(ProposedAction proposedAction, String reason) {
        this.proposedAction = proposedAction;
        this.reason = reason;
        this.startDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public ProposedAction getProposedAction() { return proposedAction; }
    public String getReason() { return reason; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
