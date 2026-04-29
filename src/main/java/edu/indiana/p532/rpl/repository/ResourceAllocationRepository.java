package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    /** All allocations for a given action ID regardless of action type. */
    List<ResourceAllocation> findByActionId(Long actionId);

    /**
     * Type-scoped query. Use this when both ProposedAction and ImplementedAction
     * allocations can exist (Week 2+), to avoid ID-collision between the two sequences.
     * Week-2 generators call findByActionIdAndActionType(id, "IMPLEMENTED_ACTION").
     */
    List<ResourceAllocation> findByActionIdAndActionType(Long actionId, String actionType);
}
