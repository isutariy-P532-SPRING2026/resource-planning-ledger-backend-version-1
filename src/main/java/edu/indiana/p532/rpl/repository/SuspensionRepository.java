package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    List<Suspension> findByProposedActionId(Long actionId);
}
