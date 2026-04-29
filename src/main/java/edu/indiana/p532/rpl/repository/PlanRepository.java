package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.plannode.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    // Top-level plans only (no parent)
    @Query("SELECT p FROM Plan p WHERE p.parent IS NULL")
    List<Plan> findTopLevel();
}
