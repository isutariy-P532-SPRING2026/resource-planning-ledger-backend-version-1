package edu.indiana.p532.rpl.domain.operational;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "implemented_actions")
public class ImplementedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false, unique = true)
    private ProposedAction proposedAction;

    @Column(name = "actual_start")
    private Instant actualStart;

    @Column(name = "actual_party")
    private String actualParty;

    @Column(name = "actual_location")
    private String actualLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status = ActionStatus.IN_PROGRESS;

    protected ImplementedAction() {}

    public ImplementedAction(ProposedAction proposedAction, Instant actualStart,
                              String actualParty, String actualLocation) {
        this.proposedAction = proposedAction;
        this.actualStart = actualStart;
        this.actualParty = actualParty;
        this.actualLocation = actualLocation;
    }

    public Long getId() { return id; }
    public ProposedAction getProposedAction() { return proposedAction; }
    public Instant getActualStart() { return actualStart; }
    public void setActualStart(Instant actualStart) { this.actualStart = actualStart; }
    public String getActualParty() { return actualParty; }
    public void setActualParty(String actualParty) { this.actualParty = actualParty; }
    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
}
