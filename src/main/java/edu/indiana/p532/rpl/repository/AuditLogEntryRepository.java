package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, Long> {
}
