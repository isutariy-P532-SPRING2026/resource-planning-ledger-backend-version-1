package edu.indiana.p532.rpl.domain.operational.plannode;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import edu.indiana.p532.rpl.repository.ResourceAllocationRepository;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.Objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Leaf node in the Composite pattern. Holds a state machine name (resolved to a
 * stateless ActionState bean by ActionStateMachineEngine at runtime).
 */
@Entity
@Table(name = "proposed_actions")
@DiscriminatorValue("ACTION")
public class ProposedAction extends PlanNodeEntity {

    @Column(name = "state_name", nullable = false)
    private String stateName = ActionStatus.PROPOSED.name();

    @Column(name = "party")
    private String party;

    @Column(name = "time_ref")
    private String timeRef;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id")
    private Protocol protocol;

    /**
     * Loaded on demand by PlanManager via ResourceAllocationRepository — NOT a JPA
     * association. Keeping this @Transient avoids the dual-mapping collision where
     * ResourceAllocation.action_id is already a plain @Column (not a FK managed by
     * a parent @OneToMany). PlanManager.loadChildrenRecursively() calls
     * setLoadedAllocations() before the entity leaves a transaction boundary.
     */
    @Transient
    private List<ResourceAllocation> loadedAllocations = new ArrayList<>();

    protected ProposedAction() {}

    public ProposedAction(String name, Protocol protocol, String party, String timeRef, String location) {
        super(name);
        this.protocol = protocol;
        this.party = party;
        this.timeRef = timeRef;
        this.location = location;
    }

    @Override
    public ActionStatus getStatus() {
        return ActionStatus.valueOf(stateName);
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        return loadedAllocations.stream()
                .filter(a -> Objects.equals(a.getResourceType().getId(), resourceType.getId()))
                .map(ResourceAllocation::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitAction(this);
    }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public String getTimeRef() { return timeRef; }
    public void setTimeRef(String timeRef) { this.timeRef = timeRef; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

        /** Returns the in-memory list populated by loadAllocations(). */
    public List<ResourceAllocation> getLoadedAllocations() {
        return Collections.unmodifiableList(loadedAllocations);
    }

    /** Called by PlanManager via the PlanNode interface — do not call directly. */
    public void setLoadedAllocations(List<ResourceAllocation> allocations) {
        this.loadedAllocations = allocations != null ? new ArrayList<>(allocations) : new ArrayList<>();
    }

    /**
     * Loads PROPOSED_ACTION allocations for this leaf from the repository.
     * Week-2 leaf types override this with their own actionType — PlanManager
     * calls loadAllocations() polymorphically so it never needs changing.
     */
    @Override
    public void loadAllocations(ResourceAllocationRepository repo) {
        setLoadedAllocations(
            repo.findByActionIdAndActionType(getId(), "PROPOSED_ACTION"));
    }
}
