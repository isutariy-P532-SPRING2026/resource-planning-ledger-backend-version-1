package edu.indiana.p532.rpl.domain.operational;

import jakarta.persistence.*;

@Entity
@Table(name = "posting_rules")
public class PostingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_account_id", nullable = false)
    private Account triggerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "output_account_id", nullable = false)
    private Account outputAccount;

    // matches a PostingRuleStrategy bean's strategyType()
    @Column(name = "strategy_type", nullable = false)
    private String strategyType;

    protected PostingRule() {}

    public PostingRule(Account triggerAccount, Account outputAccount, String strategyType) {
        this.triggerAccount = triggerAccount;
        this.outputAccount = outputAccount;
        this.strategyType = strategyType;
    }

    public Long getId() { return id; }
    public Account getTriggerAccount() { return triggerAccount; }
    public Account getOutputAccount() { return outputAccount; }
    public String getStrategyType() { return strategyType; }
}
