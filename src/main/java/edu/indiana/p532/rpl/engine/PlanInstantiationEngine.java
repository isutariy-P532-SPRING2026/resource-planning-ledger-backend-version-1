package edu.indiana.p532.rpl.engine;

import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ProtocolStep;
import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine: converts a Protocol template into a Plan with ProposedAction per step,
 * preserving the dependency structure. Algorithms are fully encapsulated here;
 * PlanManager orchestrates persistence.
 */
@Service("planInstantiationEngine")
public class PlanInstantiationEngine {

    public Plan instantiate(String planName, Protocol protocol, LocalDate targetStartDate) {
        Plan plan = new Plan(planName, protocol, targetStartDate);

        List<ProtocolStep> steps = protocol.getSteps();
        Map<String, ProposedAction> stepNameToAction = new HashMap<>();

        for (ProtocolStep step : steps) {
            ProposedAction action = new ProposedAction(
                    step.getName(),
                    step.getSubProtocol() != null ? step.getSubProtocol() : protocol,
                    null, null, null);
            plan.addChild(action);
            stepNameToAction.put(step.getName(), action);
        }

        // Dependency structure preserved in step order and dependsOn metadata.
        // Future: could store dependency edges in a separate entity if needed.
        return plan;
    }
}
