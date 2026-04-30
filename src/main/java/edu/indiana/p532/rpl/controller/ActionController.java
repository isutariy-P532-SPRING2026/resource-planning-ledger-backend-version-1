package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.dto.ImplementActionRequest;
import edu.indiana.p532.rpl.dto.ResourceAllocationRequest;
import edu.indiana.p532.rpl.dto.SuspendRequest;
import edu.indiana.p532.rpl.dto.TransitionRequest;
import edu.indiana.p532.rpl.manager.ActionManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionManager actionManager;

    public ActionController(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/implement")
    public Map<String, Object> implement(@PathVariable Long id,
                                          @RequestBody ImplementActionRequest request) {
        actionManager.implement(id, request);
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        actionManager.complete(id);
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/suspend")
    public Map<String, Object> suspend(@PathVariable Long id,
                                        @RequestBody SuspendRequest request) {
        actionManager.suspend(id, request);
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/resume")
    public Map<String, Object> resume(@PathVariable Long id) {
        actionManager.resume(id);
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/abandon")
    public Map<String, Object> abandon(@PathVariable Long id) {
        actionManager.abandon(id);
        return actionManager.getDetailAsMap(id);
    }

    /**
     * Generic transition dispatch — Week 2 new states only need a new ActionState
     * bean; no changes to Controller or Manager are required.
     */
    @PostMapping("/{id}/transition")
    public Map<String, Object> transition(@PathVariable Long id,
                                           @RequestBody TransitionRequest request) {
        actionManager.executeTransition(id, request.event(), request.safeParams());
        return actionManager.getDetailAsMap(id);
    }

    @PostMapping("/{id}/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addAllocation(@PathVariable Long id,
                                              @RequestBody ResourceAllocationRequest request) {
        var alloc = actionManager.addAllocation(id, request);
        // Return updated full detail so the UI has fresh allocations list
        return actionManager.getDetailAsMap(id);
    }
}
