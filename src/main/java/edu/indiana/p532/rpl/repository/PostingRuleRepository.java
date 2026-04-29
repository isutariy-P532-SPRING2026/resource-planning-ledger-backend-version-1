package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.operational.PostingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostingRuleRepository extends JpaRepository<PostingRule, Long> {
    List<PostingRule> findByTriggerAccountId(Long accountId);
}
