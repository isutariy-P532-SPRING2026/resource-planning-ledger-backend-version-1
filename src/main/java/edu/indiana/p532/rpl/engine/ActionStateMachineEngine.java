package edu.indiana.p532.rpl.engine;

import edu.indiana.p532.rpl.domain.operational.state.ActionState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Engine: resolves the current ActionState bean by name.
 * New states register themselves as Spring @Component — this class never changes.
 */
@Service("actionStateMachineEngine")
public class ActionStateMachineEngine {

    private final Map<String, ActionState> stateMap;

    public ActionStateMachineEngine(List<ActionState> states) {
        this.stateMap = states.stream()
                .collect(Collectors.toMap(ActionState::name, Function.identity()));
    }

    public ActionState resolve(String stateName) {
        ActionState state = stateMap.get(stateName);
        if (state == null) {
            throw new IllegalStateException("Unknown action state: " + stateName);
        }
        return state;
    }

    /** Convenience: returns the legal event names for the given state name. */
    public List<String> legalTransitions(String stateName) {
        return resolve(stateName).legalTransitions();
    }
}
