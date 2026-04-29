package edu.indiana.p532.rpl.exception;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String fromState, String event) {
        super("Illegal transition: cannot call '" + event + "' from state '" + fromState + "'");
    }
}
