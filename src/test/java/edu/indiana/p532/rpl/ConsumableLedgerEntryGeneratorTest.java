package edu.indiana.p532.rpl;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.AllocationKind;
import edu.indiana.p532.rpl.domain.ResourceKind;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.domain.operational.*;
import edu.indiana.p532.rpl.domain.operational.plannode.ProposedAction;
import edu.indiana.p532.rpl.ledger.ConsumableLedgerEntryGenerator;
import edu.indiana.p532.rpl.posting.PostingRuleEngine;
import edu.indiana.p532.rpl.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumableLedgerEntryGeneratorTest {

    @Mock private ResourceAllocationRepository allocationRepository;
    @Mock private LedgerTransactionRepository transactionRepository;
    @Mock private EntryRepository entryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PostingRuleEngine postingRuleEngine;
    @Mock private AuditLogEntryRepository auditLogEntryRepository;

    private ConsumableLedgerEntryGenerator generator;
    private ImplementedAction implementedAction;
    private ProposedAction proposedAction;

    @BeforeEach
    void setUp() {
        generator = new ConsumableLedgerEntryGenerator();
        // inject mocks via field injection (Spring @Autowired fields)
        injectField(generator, "allocationRepository", allocationRepository);
        injectField(generator, "transactionRepository", transactionRepository);
        injectField(generator, "entryRepository", entryRepository);
        injectField(generator, "accountRepository", accountRepository);
        injectField(generator, "postingRuleEngine", postingRuleEngine);
        injectField(generator, "auditLogEntryRepository", auditLogEntryRepository);

        proposedAction = new ProposedAction("Test", null, null, null, null);
        implementedAction = new ImplementedAction(proposedAction, java.time.Instant.now(), "Alice", "Lab");
    }

    @Test
    void generateEntries_consumableAllocations_createsWithdrawalAndDeposit() {
        // Arrange
        Account pool = new Account("POOL-Steel", AccountKind.POOL, 1L);
        ResourceType rt = new ResourceType("Steel", ResourceKind.CONSUMABLE, "kg");
        rt.setPoolAccount(pool);
        ResourceAllocation alloc = new ResourceAllocation(1L, rt, new BigDecimal("10"), AllocationKind.GENERAL, null, null);

        LedgerTransaction tx = new LedgerTransaction("test tx", 1L);
        when(allocationRepository.findByActionIdAndActionType(any(), any())).thenReturn(List.of(alloc));
        when(transactionRepository.save(any())).thenReturn(tx);
        when(accountRepository.findByName(anyString())).thenReturn(Optional.empty());
        Account usageAcc = new Account("USAGE-Steel-action-null", AccountKind.USAGE, 1L);
        when(accountRepository.save(any())).thenReturn(usageAcc);
        when(entryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LedgerTransaction result = generator.generateEntries(implementedAction);

        // Assert: two entries saved (withdrawal + deposit)
        verify(entryRepository, times(2)).save(any(Entry.class));
        assertNotNull(result);
    }

    @Test
    void generateEntries_assetAllocationsOnly_returnsNull() {
        // Arrange — only ASSET kind allocations, generator skips them
        Account pool = new Account("POOL-Truck", AccountKind.POOL, 2L);
        ResourceType rt = new ResourceType("Truck", ResourceKind.ASSET, "unit");
        rt.setPoolAccount(pool);
        ResourceAllocation assetAlloc = new ResourceAllocation(1L, rt, new BigDecimal("1"), AllocationKind.SPECIFIC, "TRUCK-001", null);

        when(allocationRepository.findByActionIdAndActionType(any(), any())).thenReturn(List.of(assetAlloc));

        // Act
        LedgerTransaction result = generator.generateEntries(implementedAction);

        // Assert: empty selection after CONSUMABLE filter → null returned, no entries saved
        assertNull(result);
        verify(entryRepository, never()).save(any());
    }

    @Test
    void validate_zeroQuantity_throwsIllegalArgumentException() {
        // Arrange
        Account pool = new Account("POOL-Water", AccountKind.POOL, 3L);
        ResourceType rt = new ResourceType("Water", ResourceKind.CONSUMABLE, "L");
        rt.setPoolAccount(pool);
        ResourceAllocation badAlloc = new ResourceAllocation(1L, rt, BigDecimal.ZERO, AllocationKind.GENERAL, null, null);

        when(allocationRepository.findByActionIdAndActionType(any(), any())).thenReturn(List.of(badAlloc));
        when(transactionRepository.save(any())).thenReturn(new LedgerTransaction("tx", 1L));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateEntries(implementedAction));
    }

    @Test
    void validate_negativeQuantity_throwsIllegalArgumentException() {
        // Arrange
        Account pool = new Account("POOL-Fuel", AccountKind.POOL, 4L);
        ResourceType rt = new ResourceType("Fuel", ResourceKind.CONSUMABLE, "L");
        rt.setPoolAccount(pool);
        ResourceAllocation negAlloc = new ResourceAllocation(1L, rt, new BigDecimal("-5"), AllocationKind.GENERAL, null, null);

        when(allocationRepository.findByActionIdAndActionType(any(), any())).thenReturn(List.of(negAlloc));
        when(transactionRepository.save(any())).thenReturn(new LedgerTransaction("tx", 1L));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateEntries(implementedAction));
    }

    @Test
    void appliesTo_anyAction_returnsTrue() {
        // Arrange
        ConsumableLedgerEntryGenerator gen = new ConsumableLedgerEntryGenerator();
        // Act & Assert
        assertTrue(gen.appliesTo(implementedAction));
    }

    // Helper: inject private field via reflection
    private void injectField(Object target, String fieldName, Object value) {
        try {
            // check own class first, then superclass (AbstractLedgerEntryGenerator)
            java.lang.reflect.Field field;
            try {
                field = target.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}
