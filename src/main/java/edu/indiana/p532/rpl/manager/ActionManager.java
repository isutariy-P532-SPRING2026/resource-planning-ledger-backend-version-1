package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.operational.AuditLogEntry;
import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.domain.operational.Suspension;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.domain.operational.state.ActionContext;
import edu.indiana.p532.rpl.domain.operational.state.ActionContextCallback;
import edu.indiana.p532.rpl.domain.operational.state.ActionState;
import edu.indiana.p532.rpl.dto.ImplementActionRequest;
import edu.indiana.p532.rpl.dto.ResourceAllocationRequest;
import edu.indiana.p532.rpl.dto.SuspendRequest;
import edu.indiana.p532.rpl.engine.ActionStateMachineEngine;
import edu.indiana.p532.rpl.exception.ResourceNotFoundException;
import edu.indiana.p532.rpl.repository.*;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ActionManager implements ActionContextCallback {

    private final ProposedActionRepository actionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final SuspensionRepository suspensionRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final ActionStateMachineEngine stateMachineEngine;
    private final LedgerManager ledgerManager;

    public ActionManager(ProposedActionRepository actionRepository,
                         ImplementedActionRepository implementedActionRepository,
                         SuspensionRepository suspensionRepository,
                         ResourceAllocationRepository allocationRepository,
                         ResourceTypeRepository resourceTypeRepository,
                         AuditLogEntryRepository auditLogEntryRepository,
                         ActionStateMachineEngine stateMachineEngine,
                         LedgerManager ledgerManager) {
        this.actionRepository = actionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.suspensionRepository = suspensionRepository;
        this.allocationRepository = allocationRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.stateMachineEngine = stateMachineEngine;
        this.ledgerManager = ledgerManager;
    }

    @Transactional
    public ProposedAction implement(Long actionId, ImplementActionRequest request) {
        ProposedAction action = load(actionId);
        ActionContext ctx = new ActionContext(action, this)
                .withImplementData(request.actualParty(), request.actualLocation(),
                        request.actualStart() != null ? request.actualStart() : Instant.now());
        resolveState(action).implement(ctx);
        audit("IMPLEMENT", action.getId());
        return actionRepository.save(action);
    }

    @Transactional
    public ProposedAction suspend(Long actionId, SuspendRequest request) {
        ProposedAction action = load(actionId);
        ActionContext ctx = new ActionContext(action, this)
                .withSuspensionReason(request.reason());
        resolveState(action).suspend(ctx, request.reason());
        audit("SUSPEND", action.getId());
        return actionRepository.save(action);
    }

    @Transactional
    public ProposedAction resume(Long actionId) {
        ProposedAction action = load(actionId);
        ActionContext ctx = new ActionContext(action, this);
        resolveState(action).resume(ctx);
        audit("RESUME", action.getId());
        return actionRepository.save(action);
    }

    @Transactional
    public ProposedAction complete(Long actionId) {
        ProposedAction action = load(actionId);
        ActionContext ctx = new ActionContext(action, this);
        resolveState(action).complete(ctx);
        audit("COMPLETE", action.getId());
        return actionRepository.save(action);
    }

    @Transactional
    public ProposedAction abandon(Long actionId) {
        ProposedAction action = load(actionId);
        ActionContext ctx = new ActionContext(action, this);
        resolveState(action).abandon(ctx);
        audit("ABANDON", action.getId());
        return actionRepository.save(action);
    }

    @Transactional
    public ResourceAllocation addAllocation(Long actionId, ResourceAllocationRequest request) {
        ProposedAction action = load(actionId);
        ResourceType rt = resourceTypeRepository.findById(request.resourceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ResourceType not found: " + request.resourceTypeId()));
        ResourceAllocation alloc = new ResourceAllocation(
                actionId, rt, request.quantity(), request.kind(),
                request.assetId(), request.timePeriod());
        return allocationRepository.save(alloc);
    }

    @Transactional(readOnly = true)
    public ProposedAction getById(Long id) {
        return load(id);
    }

    /** Returns the ImplementedAction for an action that has been started, or empty. */
    @Transactional(readOnly = true)
    public java.util.Optional<ImplementedAction> getImplementedAction(Long proposedActionId) {
        return implementedActionRepository.findByProposedActionId(proposedActionId);
    }

        /**
     * Returns all allocations for an action — both PROPOSED_ACTION (planned)
     * and IMPLEMENTED_ACTION (Week-2 actual asset records).
     * ActionController.toMap() calls this so the response is always complete.
     */
    @Transactional(readOnly = true)
    public java.util.List<ResourceAllocation> getAllocations(Long actionId) {
        // Fetch planned allocations (ProposedAction level)
        java.util.List<ResourceAllocation> result = new java.util.ArrayList<>(
            allocationRepository.findByActionIdAndActionType(actionId, "PROPOSED_ACTION"));

        // Fetch implemented allocations if an ImplementedAction exists (Week-2 asset records)
        implementedActionRepository.findByProposedActionId(actionId).ifPresent(impl ->
            result.addAll(
                allocationRepository.findByActionIdAndActionType(impl.getId(), "IMPLEMENTED_ACTION"))
        );

        return result;
    }

    // --- ActionContextCallback implementation ---

    @Override
    public void onImplement(ProposedAction action, String actualParty, String actualLocation, Instant actualStart) {
        ImplementedAction impl = new ImplementedAction(action, actualStart, actualParty, actualLocation);
        implementedActionRepository.save(impl);
    }

    @Override
    public void onSuspend(ProposedAction action, String reason) {
        suspensionRepository.save(new Suspension(action, reason));
    }

    @Override
    public void onComplete(ProposedAction action) {
        ImplementedAction impl = implementedActionRepository.findByProposedActionId(action.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No ImplementedAction found for action " + action.getId()));
        impl.setStatus(edu.indiana.p532.rpl.domain.ActionStatus.COMPLETED);
        implementedActionRepository.save(impl);
        ledgerManager.generateLedgerEntries(impl);
    }

    @Override
    public void onAbandon(ProposedAction action) {
        audit("ABANDON_DETAIL", action.getId());
    }

    private ProposedAction load(Long id) {
        return actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProposedAction not found: " + id));
    }

    private ActionState resolveState(ProposedAction action) {
        return stateMachineEngine.resolve(action.getStateName());
    }

    private void audit(String event, Long actionId) {
        auditLogEntryRepository.save(new AuditLogEntry(event, null, null, actionId, null));
    }
}
