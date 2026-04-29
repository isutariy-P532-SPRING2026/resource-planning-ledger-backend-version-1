package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNode;
import edu.indiana.p532.rpl.domain.operational.plannode.PlanNodeEntity;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.dto.ReportNodeDto;
import edu.indiana.p532.rpl.iterator.DepthFirstPlanIterator;
import edu.indiana.p532.rpl.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportManager {

    private final PlanManager planManager;
    private final ResourceTypeRepository resourceTypeRepository;

    public ReportManager(PlanManager planManager,
                         ResourceTypeRepository resourceTypeRepository) {
        this.planManager = planManager;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReportNodeDto> generateReport(Long planId) {
        Plan plan = planManager.getPlanWithTree(planId);

        // Fetch all known resource types once — avoids N+1 queries per node
        List<ResourceType> allResourceTypes = resourceTypeRepository.findAll();

        List<ReportNodeDto> report = new ArrayList<>();
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);

        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            boolean isLeaf = !(node instanceof Plan);

            Map<String, BigDecimal> allocations = buildAllocationsMap(node, allResourceTypes);

            report.add(new ReportNodeDto(
                    node.getId(),
                    node.getName(),
                    isLeaf ? "ACTION" : "PLAN",
                    node.getStatus().name(),
                    computeDepth(node, plan),
                    allocations));
        }
        return report;
    }

    /**
     * Computes total allocated quantity per resource type for a node (F10).
     * For PLAN nodes this sums across the entire sub-tree via getTotalAllocatedQuantity().
     * For ACTION leaves it uses the @Transient loadedAllocations populated by PlanManager.
     * Zero-quantity entries are omitted to keep the response tidy.
     */
    private Map<String, BigDecimal> buildAllocationsMap(PlanNode node, List<ResourceType> allResourceTypes) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (ResourceType rt : allResourceTypes) {
            BigDecimal total = node.getTotalAllocatedQuantity(rt);
            if (total != null && total.compareTo(BigDecimal.ZERO) != 0) {
                map.put(rt.getName(), total);
            }
        }
        return map;
    }

    private int computeDepth(PlanNode node, Plan root) {
        if (node instanceof Plan planNode) {
            if (planNode.getId().equals(root.getId())) return 0;
            Plan parent = planNode.getParent();
            return parent == null ? 0 : computeDepth(parent, root) + 1;
        }
        // ProposedAction (or any other leaf PlanNodeEntity)
        PlanNodeEntity entity = (PlanNodeEntity) node;
        Plan parent = entity.getParent();
        return parent == null ? 0 : computeDepth(parent, root) + 1;
    }
}
