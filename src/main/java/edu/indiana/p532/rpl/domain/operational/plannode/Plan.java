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

        boolean allCompleted = children.stream().allMatch(c -> c.getStatus() == ActionStatus.COMPLETED);
        if (allCompleted) return ActionStatus.COMPLETED;

        boolean allAbandoned = children.stream().allMatch(c -> c.getStatus() == ActionStatus.ABANDONED);
        if (allAbandoned) return ActionStatus.ABANDONED;

        boolean anyInProgress = children.stream().anyMatch(c ->
                c.getStatus() == ActionStatus.IN_PROGRESS || c.getStatus() == ActionStatus.COMPLETED);
        if (anyInProgress) return ActionStatus.IN_PROGRESS;

        boolean anySuspended = children.stream().anyMatch(c -> c.getStatus() == ActionStatus.SUSPENDED);
        if (anySuspended) return ActionStatus.SUSPENDED;

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
