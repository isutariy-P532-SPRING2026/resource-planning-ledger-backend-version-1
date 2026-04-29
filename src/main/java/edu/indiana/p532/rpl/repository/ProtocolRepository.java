package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
}
