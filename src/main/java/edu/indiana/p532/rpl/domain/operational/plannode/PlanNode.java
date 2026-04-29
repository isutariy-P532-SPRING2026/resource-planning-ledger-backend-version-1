package edu.indiana.p532.rpl.domain.operational.plannode;

import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import edu.indiana.p532.rpl.repository.ResourceAllocationRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PlanNode {

    Long getId();
    String getName();

    edu.indiana.p532.rpl.domain.ActionStatus getStatus();

    BigDecimal getTotalAllocatedQuantity(ResourceType resourceType);

    void accept(PlanNodeVisitor visitor);

    /**
     * Extension point: each concrete node type loads its own allocations
     * from the repository while still inside a transaction.
     * Default is a no-op so existing PLAN composites need no change.
     * New Week-2 leaf types override this — PlanManager never needs to change.
     */
    default void loadAllocations(ResourceAllocationRepository repo) {
        // PLAN composite nodes have no direct allocations — no-op by default
    }
}