package edu.indiana.p532.rpl.domain.operational.state;

import java.util.List;

/**
 * State pattern interface. Each concrete state is a stateless Spring singleton bean.
 * All mutable data lives in ActionContext (wrapping the JPA entity).
 *
 * legalTransitions() is declared here so ActionController never needs to change
 * when a new state is added in Week 2 — each state knows its own outgoing edges.
 * Adding a new state = new class + ONE existing state gains an outgoing edge.
 */
public interface ActionState {
    void implement(ActionContext ctx);
    void suspend(ActionContext ctx, String reason);
    void resume(ActionContext ctx);
    void complete(ActionContext ctx);
    void abandon(ActionContext ctx);
    String name();

    /** Returns the event names that are legal to call from this state. */
    List<String> legalTransitions();
}
