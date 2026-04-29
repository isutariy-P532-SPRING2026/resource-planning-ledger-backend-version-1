package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {
}
