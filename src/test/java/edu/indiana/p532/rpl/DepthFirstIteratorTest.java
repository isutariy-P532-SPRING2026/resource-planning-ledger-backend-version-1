package edu.indiana.p532.rpl;

import edu.indiana.p532.rpl.domain.ActionStatus;
import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNode;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.iterator.DepthFirstPlanIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepthFirstIteratorTest {

    private ProposedAction leaf(String name) {
        return new ProposedAction(name, null, null, null, null);
    }

    @Test
    void iterator_singleLeaf_returnsOneNode() {
        // Arrange
        ProposedAction leaf = leaf("leaf");
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(leaf);
        // Act & Assert
        assertTrue(it.hasNext());
        PlanNode node = it.next();
        assertEquals("leaf", node.getName());
        assertFalse(it.hasNext());
    }

    @Test
    void iterator_planWithChildren_returnsDepthFirstPreOrder() {
        // Arrange: root -> [A, subPlan -> [B, C]]
        Plan root = new Plan("root", null, null);
        ProposedAction a = leaf("A");
        Plan sub = new Plan("subPlan", null, null);
        ProposedAction b = leaf("B");
        ProposedAction c = leaf("C");
        sub.addChild(b);
        sub.addChild(c);
        root.addChild(a);
        root.addChild(sub);

        // Act
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(root);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) {
            names.add(it.next().getName());
        }

        // Assert: depth-first pre-order: root, A, subPlan, B, C
        assertEquals(List.of("root", "A", "subPlan", "B", "C"), names);
    }

    @Test
    void iterator_exhausted_throwsNoSuchElementException() {
        // Arrange
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(leaf("only"));
        it.next(); // exhaust
        // Act & Assert
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void iterator_deeplyNestedPlan_traversesAllNodes() {
        // Arrange: root -> sub1 -> sub2 -> leaf
        Plan root = new Plan("root", null, null);
        Plan sub1 = new Plan("sub1", null, null);
        Plan sub2 = new Plan("sub2", null, null);
        ProposedAction deepLeaf = leaf("deepLeaf");
        sub2.addChild(deepLeaf);
        sub1.addChild(sub2);
        root.addChild(sub1);

        // Act
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(root);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert
        assertEquals(List.of("root", "sub1", "sub2", "deepLeaf"), names);
    }
}
