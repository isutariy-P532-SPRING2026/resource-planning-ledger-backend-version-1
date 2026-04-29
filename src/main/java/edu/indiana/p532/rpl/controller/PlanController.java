package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNodeEntity;
import edu.indiana.p532.rpl.dto.CreatePlanRequest;
import edu.indiana.p532.rpl.dto.PlanNodeDto;
import edu.indiana.p532.rpl.dto.ReportNodeDto;
import edu.indiana.p532.rpl.engine.ActionStateMachineEngine;
import edu.indiana.p532.rpl.manager.PlanManager;
import edu.indiana.p532.rpl.manager.ReportManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanManager planManager;
    private final ReportManager reportManager;
    private final ActionStateMachineEngine stateMachineEngine;

    public PlanController(PlanManager planManager, ReportManager reportManager,
                           ActionStateMachineEngine stateMachineEngine) {
        this.planManager = planManager;
        this.reportManager = reportManager;
        this.stateMachineEngine = stateMachineEngine;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanNodeDto create(@RequestBody CreatePlanRequest request) {
        return toDto(planManager.createPlan(request));
    }

    @GetMapping
    public List<PlanNodeDto> listTopLevel() {
        return planManager.listTopLevel().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public PlanNodeDto get(@PathVariable Long id) {
        return toDto(planManager.getPlanWithTree(id));
    }

    @GetMapping("/{id}/report")
    public List<ReportNodeDto> report(@PathVariable Long id) {
        return reportManager.generateReport(id);
    }

    private PlanNodeDto toDto(Plan plan) {
        List<PlanNodeDto> children = plan.getChildren().stream()
                .map(child -> child instanceof Plan subPlan
                        ? toDto(subPlan)
                        : actionToDto(child))
                .toList();
        return new PlanNodeDto(plan.getId(), plan.getName(), "PLAN",
                plan.getStatus().name(), List.of(), children);
    }

    private PlanNodeDto actionToDto(PlanNodeEntity child) {
        // legalTransitions sourced from the state bean — no hardcoding; new states auto-appear
        List<String> transitions = stateMachineEngine.legalTransitions(child.getStatus().name());
        return new PlanNodeDto(child.getId(), child.getName(), "ACTION",
                child.getStatus().name(), transitions, List.of());
    }
}
