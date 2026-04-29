package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.knowledge.ProtocolStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolStepRepository extends JpaRepository<ProtocolStep, Long> {
    List<ProtocolStep> findByProtocolIdOrderByStepOrderAsc(Long protocolId);
}
