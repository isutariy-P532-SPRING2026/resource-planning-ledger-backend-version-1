package edu.indiana.p532.rpl.domain.operational.plannode;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Abstract JPA base for the Composite pattern tree.
 * JOINED inheritance: plan_nodes (base), plans, proposed_actions each have their own table.
 * parent_plan_id provides the self-join for the tree structure.
 */
@Entity
@Table(name = "plan_nodes")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
public abstract class PlanNodeEntity implements PlanNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_plan_id")
    private Plan parent;

    protected PlanNodeEntity() {}

    protected PlanNodeEntity(String name) {
        this.name = name;
    }

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Plan getParent() { return parent; }
    public void setParent(Plan parent) { this.parent = parent; }

    @Override
    public abstract ActionStatus getStatus();

    @Override
    public abstract BigDecimal getTotalAllocatedQuantity(ResourceType resourceType);

    @Override
    public abstract void accept(PlanNodeVisitor visitor);
}
