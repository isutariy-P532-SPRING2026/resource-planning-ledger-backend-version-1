package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.domain.operational.ResourceAllocation;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.dto.ImplementActionRequest;
import edu.indiana.p532.rpl.dto.ResourceAllocationRequest;
import edu.indiana.p532.rpl.dto.SuspendRequest;
import edu.indiana.p532.rpl.engine.ActionStateMachineEngine;
import edu.indiana.p532.rpl.manager.ActionManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionManager actionManager;
    private final ActionStateMachineEngine stateMachineEngine;

    public ActionController(ActionManager actionManager,
                             ActionStateMachineEngine stateMachineEngine) {
        this.actionManager = actionManager;
        this.stateMachineEngine = stateMachineEngine;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return toMap(actionManager.getById(id));
    }

    @PostMapping("/{id}/implement")
    public Map<String, Object> implement(@PathVariable Long id,
                                          @RequestBody ImplementActionRequest request) {
        return toMap(actionManager.implement(id, request));
    }

    @PostMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        return toMap(actionManager.complete(id));
    }

    @PostMapping("/{id}/suspend")
    public Map<String, Object> suspend(@PathVariable Long id,
                                        @RequestBody SuspendRequest request) {
        return toMap(actionManager.suspend(id, request));
    }

    @PostMapping("/{id}/resume")
    public Map<String, Object> resume(@PathVariable Long id) {
        return toMap(actionManager.resume(id));
    }

    @PostMapping("/{id}/abandon")
    public Map<String, Object> abandon(@PathVariable Long id) {
        return toMap(actionManager.abandon(id));
    }

    @PostMapping("/{id}/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addAllocation(@PathVariable Long id,
                                              @RequestBody ResourceAllocationRequest request) {
        var alloc = actionManager.addAllocation(id, request);
        return Map.of(
                "id", alloc.getId(),
                "actionId", alloc.getActionId(),
                "resourceTypeId", alloc.getResourceType().getId(),
                "quantity", alloc.getQuantity(),
                "kind", alloc.getKind().name()
        );
    }

    private Map<String, Object> toMap(ProposedAction a) {
        // --- core fields (F3/F6) ---
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",              a.getId());
        map.put("name",            a.getName());
        map.put("status",          a.getStatus().name());
        map.put("party",           a.getParty()    != null ? a.getParty()    : "");
        map.put("timeRef",         a.getTimeRef()  != null ? a.getTimeRef()  : "");
        map.put("location",        a.getLocation() != null ? a.getLocation() : "");
        // State-driven transitions — new states need zero controller changes (legalTransitions() on interface)
        map.put("legalTransitions", stateMachineEngine.legalTransitions(a.getStatus().name()));

        // --- allocations (F6) ---
        List<ResourceAllocation> allocs = actionManager.getAllocations(a.getId());
        List<Map<String, Object>> allocList = allocs.stream().map(al -> {
            Map<String, Object> am = new LinkedHashMap<>();
            am.put("id",               al.getId());
            am.put("resourceTypeName", al.getResourceType().getName());
            am.put("quantity",         al.getQuantity());
            am.put("kind",             al.getKind().name());
            am.put("assetId",          al.getAssetId()    != null ? al.getAssetId()    : "");
            am.put("timePeriod",       al.getTimePeriod() != null ? al.getTimePeriod() : "");
            return am;
        }).toList();
        map.put("allocations", allocList);

        // --- implemented diff (F5) — null when action has not been started yet ---
        Optional<ImplementedAction> implOpt = actionManager.getImplementedAction(a.getId());
        if (implOpt.isPresent()) {
            ImplementedAction impl = implOpt.get();
            Map<String, Object> implMap = new LinkedHashMap<>();
            implMap.put("actualParty",    impl.getActualParty()    != null ? impl.getActualParty()    : "");
            implMap.put("actualLocation", impl.getActualLocation() != null ? impl.getActualLocation() : "");
            implMap.put("actualStart",    impl.getActualStart()    != null ? impl.getActualStart().toString() : "");
            implMap.put("status",         impl.getStatus()         != null ? impl.getStatus().name()  : "");
            map.put("implemented", implMap);
        } else {
            map.put("implemented", null);
        }

        return map;
    }
}
