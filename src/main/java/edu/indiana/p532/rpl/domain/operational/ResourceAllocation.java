package edu.indiana.p532.rpl.domain.operational;

import edu.indiana.p532.rpl.domain.AllocationKind;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "resource_allocations")
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // poly: type discriminator prevents ID collision between ProposedAction (plan_nodes sequence)
    // and ImplementedAction (own sequence) when Week 2 adds ImplementedAction allocations.
    // Week 2 new generators just call findByActionIdAndActionType("IMPLEMENTED_ACTION").
    @Column(name = "action_type", nullable = false)
    private String actionType = "PROPOSED_ACTION";

    @Column(name = "action_id", nullable = false)
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationKind kind;

    // only for SPECIFIC allocations
    @Column(name = "asset_id")
    private String assetId;

    // ISO-8601 period string, e.g. "2026-01-01/2026-12-31"
    @Column(name = "time_period")
    private String timePeriod;

    protected ResourceAllocation() {}

    public ResourceAllocation(Long actionId, ResourceType resourceType, BigDecimal quantity,
                               AllocationKind kind, String assetId, String timePeriod) {
        this.actionType = "PROPOSED_ACTION";
        this.actionId = actionId;
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.kind = kind;
        this.assetId = assetId;
        this.timePeriod = timePeriod;
    }

    public Long getId() { return id; }
    public String getActionType() { return actionType; }
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public AllocationKind getKind() { return kind; }
    public void setKind(AllocationKind kind) { this.kind = kind; }
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
    public String getTimePeriod() { return timePeriod; }
    public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }
}
