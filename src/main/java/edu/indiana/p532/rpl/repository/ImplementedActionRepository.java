package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImplementedActionRepository extends JpaRepository<ImplementedAction, Long> {
    Optional<ImplementedAction> findByProposedActionId(Long proposedActionId);
}
