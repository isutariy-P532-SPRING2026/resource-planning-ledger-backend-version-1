package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.engine.LedgerEngine;
import edu.indiana.p532.rpl.repository.AccountRepository;
import edu.indiana.p532.rpl.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LedgerManager {

    private final LedgerEngine ledgerEngine;
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    public LedgerManager(LedgerEngine ledgerEngine,
                          AccountRepository accountRepository,
                          EntryRepository entryRepository) {
        this.ledgerEngine = ledgerEngine;
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
    }

    @Transactional
    public void generateLedgerEntries(ImplementedAction action) {
        ledgerEngine.generateAllEntries(action);
    }

    @Transactional(readOnly = true)
    public List<Entry> getEntriesForAccount(Long accountId) {
        return entryRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long accountId) {
        BigDecimal balance = entryRepository.sumAmountByAccountId(accountId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Account> getAllPoolAccounts() {
        return accountRepository.findByKind(AccountKind.POOL);
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
