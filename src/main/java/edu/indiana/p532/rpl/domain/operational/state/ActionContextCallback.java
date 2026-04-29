package edu.indiana.p532.rpl.domain.operational.state;

import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;

import java.time.Instant;

/**
 * Callback interface implemented by ActionManager.
 * Decouples ActionContext from the concrete manager class, keeping states testable
 * without a Spring context by substituting a test double for this interface.
 */
public interface ActionContextCallback {
    void onImplement(ProposedAction action, String actualParty, String actualLocation, Instant actualStart);
    void onSuspend(ProposedAction action, String reason);
    void onComplete(ProposedAction action);
    void onAbandon(ProposedAction action);
}
