package edu.indiana.p532.rpl.engine;

import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ProtocolStep;
import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Engine: converts a Protocol template into a Plan with ProposedAction per step,
 * preserving the dependency structure. Algorithms are fully encapsulated here;
 * PlanManager orchestrates persistence.
 */
@Service("planInstantiationEngine")
public class PlanInstantiationEngine {

    public Plan instantiate(String planName, Protocol protocol, LocalDate targetStartDate) {
        Plan plan = new Plan(planName, protocol, targetStartDate);
        expandSteps(plan, protocol);
        return plan;
    }

    /**
     * Recursively expands each step of the given protocol into the parent plan.
     * Steps with a sub-protocol become nested Plan nodes (composite); plain steps
     * become ProposedAction leaf nodes.
     */
    private void expandSteps(Plan parent, Protocol protocol) {
        for (ProtocolStep step : protocol.getSteps()) {
            if (step.getSubProtocol() != null) {
                Plan subPlan = new Plan(step.getName(), step.getSubProtocol(), null);
                expandSteps(subPlan, step.getSubProtocol());
                parent.addChild(subPlan);
            } else {
                ProposedAction action = new ProposedAction(
                        step.getName(), protocol, null, null, null);
                action.setDependsOn(step.getDependsOn());
                parent.addChild(action);
            }
        }
    }
}
