package edu.indiana.p532.rpl.posting;

import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.domain.operational.PostingRule;

/**
 * Strategy extension point for posting rules. New strategies in Week 2 are new
 * Spring @Component classes — PostingRuleEngine discovers them via List<> injection.
 */
public interface PostingRuleStrategy {
    String strategyType();
    void execute(PostingRule rule, Entry triggeringEntry, Account triggerAccount);
}
