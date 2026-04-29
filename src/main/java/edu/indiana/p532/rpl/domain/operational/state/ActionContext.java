package edu.indiana.p532.rpl.domain.operational.state;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;

import java.time.Instant;

/**
 * Carries both the mutable entity and any request-time data needed for a transition.
 * States call back through this context rather than directly into Manager beans,
 * keeping state objects framework-agnostic and fully unit-testable.
 */
public class ActionContext {

    private final ProposedAction action;
    private final ActionContextCallback callback;

    // Request-time data (populated before a transition is triggered)
    private String actualParty;
    private String actualLocation;
    private Instant actualStart;
    private String suspensionReason;

    public ActionContext(ProposedAction action, ActionContextCallback callback) {
        this.action = action;
        this.callback = callback;
    }

    public ProposedAction getAction() { return action; }

    public void transitionTo(ActionStatus newStatus) {
        action.setStateName(newStatus.name());
    }

    public void createImplementedAction() {
        callback.onImplement(action, actualParty, actualLocation, actualStart);
    }

    public void recordSuspension() {
        callback.onSuspend(action, suspensionReason);
    }

    public void generateLedgerEntries() {
        callback.onComplete(action);
    }

    public void recordAbandon() {
        callback.onAbandon(action);
    }

    // --- fluent setters ---
    public ActionContext withImplementData(String party, String location, Instant start) {
        this.actualParty = party;
        this.actualLocation = location;
        this.actualStart = start != null ? start : Instant.now();
        return this;
    }

    public ActionContext withSuspensionReason(String reason) {
        this.suspensionReason = reason;
        return this;
    }

    public String getActualParty() { return actualParty; }
    public String getActualLocation() { return actualLocation; }
    public Instant getActualStart() { return actualStart; }
    public String getSuspensionReason() { return suspensionReason; }
}
