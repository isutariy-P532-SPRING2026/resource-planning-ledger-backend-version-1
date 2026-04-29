package edu.indiana.p532.rpl.posting;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.AuditLogEntry;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.domain.operational.PostingRule;
import edu.indiana.p532.rpl.repository.AccountRepository;
import edu.indiana.p532.rpl.repository.AuditLogEntryRepository;
import edu.indiana.p532.rpl.repository.EntryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class OverConsumptionAlertStrategy implements PostingRuleStrategy {

    private final EntryRepository entryRepository;
    private final AccountRepository accountRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;

    public OverConsumptionAlertStrategy(EntryRepository entryRepository,
                                         AccountRepository accountRepository,
                                         AuditLogEntryRepository auditLogEntryRepository) {
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
    }

    @Override
    public String strategyType() {
        return "OVER_CONSUMPTION_ALERT";
    }

    @Override
    public void execute(PostingRule rule, Entry triggeringEntry, Account triggerAccount) {
        BigDecimal balance = entryRepository.sumAmountByAccountId(triggerAccount.getId());
        if (balance == null) balance = BigDecimal.ZERO;

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            Account alertAccount = rule.getOutputAccount();
            Entry alertEntry = new Entry(
                    triggeringEntry.getTransaction(),
                    alertAccount,
                    BigDecimal.ZERO,
                    Instant.now(),
                    Instant.now(),
                    "ALERT: pool account " + triggerAccount.getName()
                            + " balance below zero: " + balance);
            entryRepository.save(alertEntry);

            auditLogEntryRepository.save(new AuditLogEntry(
                    "OVER_CONSUMPTION_ALERT",
                    triggerAccount.getId(),
                    alertEntry.getId(),
                    null,
                    "balance=" + balance));
        }
    }
}
