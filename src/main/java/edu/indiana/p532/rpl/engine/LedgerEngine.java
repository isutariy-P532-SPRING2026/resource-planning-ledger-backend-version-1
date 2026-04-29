package edu.indiana.p532.rpl.engine;

import edu.indiana.p532.rpl.domain.operational.ImplementedAction;
import edu.indiana.p532.rpl.domain.operational.LedgerTransaction;
import edu.indiana.p532.rpl.ledger.AbstractLedgerEntryGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Engine: delegates to all registered LedgerEntryGenerators that apply to this action.
 * Week 2 adds AssetLedgerEntryGenerator as a new @Component — this class never changes.
 */
@Service("ledgerEngine")
public class LedgerEngine {

    private final List<AbstractLedgerEntryGenerator> generators;

    public LedgerEngine(List<AbstractLedgerEntryGenerator> generators) {
        this.generators = generators;
    }

    public void generateAllEntries(ImplementedAction action) {
        for (AbstractLedgerEntryGenerator generator : generators) {
            if (generator.appliesTo(action)) {
                generator.generateEntries(action);
            }
        }
    }
}
