package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByName(String name);
    List<Account> findByKind(AccountKind kind);
}
