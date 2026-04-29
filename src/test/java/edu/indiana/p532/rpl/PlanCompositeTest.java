package edu.indiana.p532.rpl;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.ResourceKind;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import edu.indiana.p532.rpl.domain.AllocationKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlanCompositeTest {

    private ProposedAction actionWithStatus(ActionStatus status) {
        ProposedAction a = new ProposedAction("action-" + status, null, null, null, null);
        a.setStateName(status.name());
        return a;
    }

    @Test
    void getStatus_allChildrenCompleted_returnsCompleted() {
        // Arrange
        Plan plan = new Plan("Test Plan", null, null);
        plan.addChild(actionWithStatus(ActionStatus.COMPLETED));
        plan.addChild(actionWithStatus(ActionStatus.COMPLETED));
        // Act & Assert
        assertEquals(ActionStatus.COMPLETED, plan.getStatus());
    }

    @Test
    void getStatus_anyChildInProgress_returnsInProgress() {
        // Arrange
        Plan plan = new Plan("Test Plan", null, null);
        plan.addChild(actionWithStatus(ActionStatus.IN_PROGRESS));
        plan.addChild(actionWithStatus(ActionStatus.PROPOSED));
        // Act & Assert
        assertEquals(ActionStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    void getStatus_completedMixedWithProposed_returnsInProgress() {
        // Arrange — one completed counts as "any in progress or completed but not all"
        Plan plan = new Plan("Test Plan", null, null);
        plan.addChild(actionWithStatus(ActionStatus.COMPLETED));
        plan.addChild(actionWithStatus(ActionStatus.PROPOSED));
        // Act & Assert
        assertEquals(ActionStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    void getStatus_anyChildSuspendedNoneInProgress_returnsSuspended() {
        // Arrange
        Plan plan = new Plan("Test Plan", null, null);
        plan.addChild(actionWithStatus(ActionStatus.SUSPENDED));
        plan.addChild(actionWithStatus(ActionStatus.PROPOSED));
        // Act & Assert
        assertEquals(ActionStatus.SUSPENDED, plan.getStatus());
    }

    @Test
    void getStatus_allChildrenAbandoned_returnsAbandoned() {
        // Arrange
        Plan plan = new Plan("Test Plan", null, null);
        plan.addChild(actionWithStatus(ActionStatus.ABANDONED));
        plan.addChild(actionWithStatus(ActionStatus.ABANDONED));
        // Act & Assert
        assertEquals(ActionStatus.ABANDONED, plan.getStatus());
    }

    @Test
    void getStatus_emptyPlan_returnsProposed() {
        // Arrange
        Plan plan = new Plan("Empty Plan", null, null);
        // Act & Assert
        assertEquals(ActionStatus.PROPOSED, plan.getStatus());
    }

    @Test
    void getTotalAllocatedQuantity_recursiveSumOverLeaves() {
        // Arrange
        ResourceType rt = new ResourceType("Steel", ResourceKind.CONSUMABLE, "kg");
        Plan subPlan = new Plan("Sub", null, null);
        ProposedAction a1 = actionWithStatus(ActionStatus.PROPOSED);
        ProposedAction a2 = actionWithStatus(ActionStatus.PROPOSED);

        // Use setLoadedAllocations() — the @Transient field populated by PlanManager at runtime
        ResourceAllocation alloc1 = new ResourceAllocation(1L, rt, new BigDecimal("10"), AllocationKind.GENERAL, null, null);
        ResourceAllocation alloc2 = new ResourceAllocation(2L, rt, new BigDecimal("5"), AllocationKind.GENERAL, null, null);
        a1.setLoadedAllocations(java.util.List.of(alloc1));
        a2.setLoadedAllocations(java.util.List.of(alloc2));

        subPlan.addChild(a1);
        Plan root = new Plan("Root", null, null);
        root.addChild(subPlan);
        root.addChild(a2);

        // Act
        BigDecimal total = root.getTotalAllocatedQuantity(rt);

        // Assert
        assertEquals(new BigDecimal("15"), total);
    }
}
