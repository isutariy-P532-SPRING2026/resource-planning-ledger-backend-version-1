package edu.indiana.p532.rpl.posting;

import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.domain.operational.PostingRule;
import edu.indiana.p532.rpl.repository.PostingRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Engine layer: applies matching PostingRuleStrategy for each rule on the trigger account.
 * New strategies register themselves via Spring's List injection — this class never changes.
 */
@Service("postingRuleEngine")
public class PostingRuleEngine {

    private final PostingRuleRepository postingRuleRepository;
    private final Map<String, PostingRuleStrategy> strategyMap;

    public PostingRuleEngine(PostingRuleRepository postingRuleRepository,
                              List<PostingRuleStrategy> strategies) {
        this.postingRuleRepository = postingRuleRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(PostingRuleStrategy::strategyType, Function.identity()));
    }

    public void applyRules(Entry triggeringEntry, Account account) {
        if (account == null) return;
        List<PostingRule> rules = postingRuleRepository.findByTriggerAccountId(account.getId());
        for (PostingRule rule : rules) {
            PostingRuleStrategy strategy = strategyMap.get(rule.getStrategyType());
            if (strategy != null) {
                strategy.execute(rule, triggeringEntry, account);
            }
        }
    }
}
