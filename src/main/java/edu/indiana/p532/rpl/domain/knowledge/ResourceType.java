package edu.indiana.p532.rpl.domain.knowledge;

import edu.indiana.p532.rpl.domain.ResourceKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import jakarta.persistence.*;

@Entity
@Table(name = "resource_types")
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceKind kind;

    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pool_account_id")
    private Account poolAccount;

    protected ResourceType() {}

    public ResourceType(String name, ResourceKind kind, String unitOfMeasure) {
        this.name = name;
        this.kind = kind;
        this.unitOfMeasure = unitOfMeasure;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceKind getKind() { return kind; }
    public void setKind(ResourceKind kind) { this.kind = kind; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Account getPoolAccount() { return poolAccount; }
    public void setPoolAccount(Account poolAccount) { this.poolAccount = poolAccount; }
}
