package edu.indiana.p532.rpl.domain.operational;

import edu.indiana.p532.rpl.domain.AccountKind;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountKind kind;

    // resource type id for display — nullable (alert memo accounts may be untyped)
    @Column(name = "resource_type_id")
    private Long resourceTypeId;

    protected Account() {}

    public Account(String name, AccountKind kind, Long resourceTypeId) {
        this.name = name;
        this.kind = kind;
        this.resourceTypeId = resourceTypeId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AccountKind getKind() { return kind; }
    public void setKind(AccountKind kind) { this.kind = kind; }
    public Long getResourceTypeId() { return resourceTypeId; }
    public void setResourceTypeId(Long resourceTypeId) { this.resourceTypeId = resourceTypeId; }
}
