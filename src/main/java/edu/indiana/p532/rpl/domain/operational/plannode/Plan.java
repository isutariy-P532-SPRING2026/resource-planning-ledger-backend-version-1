package edu.indiana.p532.rpl.domain.operational.plannode;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite node. getStatus() is derived from children per spec rules.
 * getTotalAllocatedQuantity() recurses through all descendants.
 */
@Entity
@Table(name = "plans")
@DiscriminatorValue("PLAN")
public class Plan extends PlanNodeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_protocol_id")
    private Protocol sourceProtocol;

    @Column(name = "target_start_date")
    private LocalDate targetStartDate;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "child_order")
    private List<PlanNodeEntity> children = new ArrayList<>();

    protected Plan() {}

    public Plan(String name, Protocol sourceProtocol, LocalDate targetStartDate) {
        super(name);
        this.sourceProtocol = sourceProtocol;
        this.targetStartDate = targetStartDate;
    }

    @Override
    public ActionStatus getStatus() {
        if (children.isEmpty()) return ActionStatus.PROPOSED;

        boolean hasProposed = false;
        boolean hasInProgress = false;
        boolean hasCompleted = false;
        boolean hasSuspended = false;
        boolean hasAbandoned = false;

        for (PlanNodeEntity child : children) {
            ActionStatus status = child.getStatus();

            switch (status) {
                case PROPOSED:
                    hasProposed = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
                case COMPLETED:
                    hasCompleted = true;
                    break;
                case SUSPENDED:
                    hasSuspended = true;
                    break;
                case ABANDONED:
                    hasAbandoned = true;
                    break;
            }
        }

        // 1. Any IN_PROGRESS → IN_PROGRESS
        if (hasInProgress) return ActionStatus.IN_PROGRESS;

        // 2. Mixed COMPLETED + PROPOSED → IN_PROGRESS  ✅ (this fixes your failing test)
        if (hasCompleted && hasProposed) return ActionStatus.IN_PROGRESS;

        // 3. All COMPLETED → COMPLETED
        if (hasCompleted && !hasProposed && !hasSuspended && !hasAbandoned)
            return ActionStatus.COMPLETED;

        // 4. Any SUSPENDED (and none IN_PROGRESS) → SUSPENDED
        if (hasSuspended) return ActionStatus.SUSPENDED;

        // 5. All ABANDONED → ABANDONED
        if (hasAbandoned && !hasProposed && !hasCompleted && !hasInProgress)
            return ActionStatus.ABANDONED;

        // 6. Default → PROPOSED
        return ActionStatus.PROPOSED;
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        return children.stream()
                .map(c -> c.getTotalAllocatedQuantity(resourceType))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitPlan(this);
    }

    public void addChild(PlanNodeEntity child) {
        child.setParent(this);
        children.add(child);
    }

    public List<PlanNodeEntity> getChildren() { return children; }
    public Protocol getSourceProtocol() { return sourceProtocol; }
    public void setSourceProtocol(Protocol sourceProtocol) { this.sourceProtocol = sourceProtocol; }
    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }
}
