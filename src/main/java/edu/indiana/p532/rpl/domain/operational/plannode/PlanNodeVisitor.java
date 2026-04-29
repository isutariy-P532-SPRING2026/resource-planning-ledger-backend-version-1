package edu.indiana.p532.rpl.domain.operational.plannode;

/**
 * Visitor extension point — new visitors in Week 2 add new implementing classes;
 * existing node classes never change.
 */
public interface PlanNodeVisitor {
    void visitPlan(Plan plan);
    void visitAction(ProposedAction action);
}
