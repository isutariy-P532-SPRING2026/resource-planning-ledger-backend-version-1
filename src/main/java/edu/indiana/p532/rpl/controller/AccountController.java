package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.operational.Entry;
import edu.indiana.p532.rpl.dto.AccountDto;
import edu.indiana.p532.rpl.dto.EntryDto;
import edu.indiana.p532.rpl.manager.LedgerManager;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final LedgerManager ledgerManager;

    public AccountController(LedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }

    @GetMapping
    public List<AccountDto> listAccounts() {
        return ledgerManager.getAllAccounts().stream().map(acc -> {
            BigDecimal balance = ledgerManager.getBalance(acc.getId());
            return new AccountDto(acc.getId(), acc.getName(), acc.getKind().name(),
                    balance, balance.compareTo(BigDecimal.ZERO) < 0);
        }).toList();
    }

    @PostMapping("/{id}/deposit")
    public AccountDto deposit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = body.getOrDefault("description", "Stock deposit").toString();
        ledgerManager.depositToPool(id, amount, description);
        Account acc = ledgerManager.getAccountById(id);
        BigDecimal balance = ledgerManager.getBalance(acc.getId());
        return new AccountDto(acc.getId(), acc.getName(), acc.getKind().name(),
                balance, balance.compareTo(BigDecimal.ZERO) < 0);
    }

    @GetMapping("/{id}/entries")
    public List<EntryDto> getEntries(@PathVariable Long id) {
        return ledgerManager.getEntriesForAccount(id).stream()
                .map(this::toDto)
                .toList();
    }

    private EntryDto toDto(Entry e) {
        return new EntryDto(
                e.getId(),
                e.getAccount().getId(),
                e.getAccount().getName(),
                e.getAmount(),
                e.getChargedAt().toString(),
                e.getBookedAt().toString(),
                e.getTransaction().getOriginatingActionId(),
                e.getDescription()
        );
    }
}
