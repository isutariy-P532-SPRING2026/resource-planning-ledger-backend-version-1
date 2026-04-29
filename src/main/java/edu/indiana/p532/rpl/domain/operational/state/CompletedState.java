package edu.indiana.p532.rpl.domain.operational.state;

import edu.indiana.p532.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompletedState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException(name(), "suspend");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "resume");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "complete");
    }

    @Override
    public void abandon(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "abandon");
    }

    @Override
    public String name() { return "COMPLETED"; }

    @Override
    public List<String> legalTransitions() { return List.of(); }
}
