package edu.indiana.p532.rpl.ledger;

import edu.indiana.p532.rpl.domain.ResourceKind;
import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import edu.indiana.p532.rpl.repository.ResourceAllocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Concrete Template Method subclass for consumable resource allocations.
 * Week 2 adds AssetLedgerEntryGenerator as a separate subclass — zero changes here.
 */
@Component
public class ConsumableLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Override
    public boolean appliesTo(ImplementedAction action) {
        return true; // always checked; selectAllocations filters to consumables only
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        // Use type-scoped query to avoid ID collision with ImplementedAction allocations (Week 2+)
        return allocationRepository.findByActionIdAndActionType(
                        action.getProposedAction().getId(), "PROPOSED_ACTION")
                .stream()
                .filter(a -> a.getResourceType().getKind() == ResourceKind.CONSUMABLE)
                .toList();
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation a : allocs) {
            if (a.getQuantity() == null || a.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Consumable allocation must have positive quantity, got: " + a.getQuantity());
            }
        }
    }
}
