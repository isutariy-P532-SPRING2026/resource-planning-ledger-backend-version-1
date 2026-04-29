package edu.indiana.p532.rpl;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.domain.operational.state.*;
import edu.indiana.p532.rpl.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionStateMachineTest {

    @Mock
    private ActionContextCallback callback;

    private ProposedAction action;
    private ActionContext ctx;

    @BeforeEach
    void setUp() {
        // Arrange: fresh PROPOSED action and context before each test
        action = new ProposedAction("Test Action", null, "Alice", null, "Lab A");
        ctx = new ActionContext(action, callback)
                .withImplementData("Alice", "Lab A", Instant.now());
    }

    // --- Legal transitions ---

    @Test
    void implement_proposedState_transitionsToInProgress() {
        // Arrange
        ProposedState state = new ProposedState();
        // Act
        state.implement(ctx);
        // Assert
        assertEquals(ActionStatus.IN_PROGRESS.name(), action.getStateName());
        verify(callback).onImplement(eq(action), any(), any(), any());
    }

    @Test
    void suspend_proposedState_transitionsToSuspended() {
        // Arrange
        ProposedState state = new ProposedState();
        ctx.withSuspensionReason("Awaiting parts");
        // Act
        state.suspend(ctx, "Awaiting parts");
        // Assert
        assertEquals(ActionStatus.SUSPENDED.name(), action.getStateName());
        verify(callback).onSuspend(action, "Awaiting parts");
    }

    @Test
    void abandon_proposedState_transitionsToAbandoned() {
        // Arrange
        ProposedState state = new ProposedState();
        // Act
        state.abandon(ctx);
        // Assert
        assertEquals(ActionStatus.ABANDONED.name(), action.getStateName());
    }

    @Test
    void resume_suspendedState_transitionsToProposed() {
        // Arrange
        action.setStateName(ActionStatus.SUSPENDED.name());
        SuspendedState state = new SuspendedState();
        // Act
        state.resume(ctx);
        // Assert
        assertEquals(ActionStatus.PROPOSED.name(), action.getStateName());
    }

    @Test
    void abandon_suspendedState_transitionsToAbandoned() {
        // Arrange
        action.setStateName(ActionStatus.SUSPENDED.name());
        SuspendedState state = new SuspendedState();
        // Act
        state.abandon(ctx);
        // Assert
        assertEquals(ActionStatus.ABANDONED.name(), action.getStateName());
    }

    @Test
    void complete_inProgressState_transitionsToCompletedAndFiresLedger() {
        // Arrange
        action.setStateName(ActionStatus.IN_PROGRESS.name());
        InProgressState state = new InProgressState();
        // Act
        state.complete(ctx);
        // Assert
        assertEquals(ActionStatus.COMPLETED.name(), action.getStateName());
        verify(callback).onComplete(action);
    }

    @Test
    void suspend_inProgressState_transitionsToSuspended() {
        // Arrange
        action.setStateName(ActionStatus.IN_PROGRESS.name());
        InProgressState state = new InProgressState();
        ctx.withSuspensionReason("Resource unavailable");
        // Act
        state.suspend(ctx, "Resource unavailable");
        // Assert
        assertEquals(ActionStatus.SUSPENDED.name(), action.getStateName());
        verify(callback).onSuspend(action, "Resource unavailable");
    }

    // --- Illegal transitions ---

    @Test
    void implement_completedState_throwsIllegalStateTransitionException() {
        // Arrange
        action.setStateName(ActionStatus.COMPLETED.name());
        CompletedState state = new CompletedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.implement(ctx));
    }

    @Test
    void suspend_completedState_throwsIllegalStateTransitionException() {
        // Arrange
        CompletedState state = new CompletedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.suspend(ctx, "reason"));
    }

    @Test
    void abandon_completedState_throwsIllegalStateTransitionException() {
        // Arrange
        CompletedState state = new CompletedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.abandon(ctx));
    }

    @Test
    void implement_abandonedState_throwsIllegalStateTransitionException() {
        // Arrange
        AbandonedState state = new AbandonedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.implement(ctx));
    }

    @Test
    void resume_inProgressState_throwsIllegalStateTransitionException() {
        // Arrange
        action.setStateName(ActionStatus.IN_PROGRESS.name());
        InProgressState state = new InProgressState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.resume(ctx));
    }

    @Test
    void suspend_suspendedState_throwsIllegalStateTransitionException() {
        // Arrange
        action.setStateName(ActionStatus.SUSPENDED.name());
        SuspendedState state = new SuspendedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.suspend(ctx, "reason"));
    }

    @Test
    void implement_suspendedState_throwsIllegalStateTransitionException() {
        // Arrange
        action.setStateName(ActionStatus.SUSPENDED.name());
        SuspendedState state = new SuspendedState();
        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.implement(ctx));
    }
}
