package edu.indiana.p532.rpl.iterator;

import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNode;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNodeEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator pattern: pure-Java depth-first pre-order traversal over in-memory PlanNode tree.
 * No JPA queries inside next(). PlanManager must load the subtree before constructing this.
 * Clients must use this iterator; manual child recursion is prohibited.
 */
public class DepthFirstPlanIterator implements Iterator<PlanNode> {

    private final Deque<PlanNode> stack = new ArrayDeque<>();

    public DepthFirstPlanIterator(PlanNode root) {
        stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) throw new NoSuchElementException();
        PlanNode node = stack.pop();
        if (node instanceof Plan plan) {
            List<PlanNodeEntity> children = plan.getChildren();
            // push in reverse so first child is processed first
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
        return node;
    }
}
