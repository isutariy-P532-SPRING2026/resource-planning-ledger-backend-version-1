package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.AuditLogEntry;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.domain.operational.LedgerTransaction;
import edu.indiana.p532.rpl.engine.LedgerEngine;
import edu.indiana.p532.rpl.exception.ResourceNotFoundException;
import edu.indiana.p532.rpl.repository.AccountRepository;
import edu.indiana.p532.rpl.repository.AuditLogEntryRepository;
import edu.indiana.p532.rpl.repository.EntryRepository;
import edu.indiana.p532.rpl.repository.LedgerTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class LedgerManager {

    private final LedgerEngine ledgerEngine;
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;

    public LedgerManager(LedgerEngine ledgerEngine,
                          AccountRepository accountRepository,
                          EntryRepository entryRepository,
                          LedgerTransactionRepository transactionRepository,
                          AuditLogEntryRepository auditLogEntryRepository) {
        this.ledgerEngine = ledgerEngine;
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
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

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    /** Credit a pool account with an initial (or additional) stock quantity. */
    @Transactional
    public void depositToPool(Long accountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        LedgerTransaction tx = transactionRepository.save(
                new LedgerTransaction("Stock deposit: " + description, null));
        Instant now = Instant.now();
        Entry entry = new Entry(tx, account, amount, now, now, description);
        Entry saved = entryRepository.save(entry);
        auditLogEntryRepository.save(new AuditLogEntry(
                "DEPOSIT", account.getId(), saved.getId(), null, "amount=" + amount));
    }
}
