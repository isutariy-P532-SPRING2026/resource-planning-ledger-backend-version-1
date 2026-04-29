package edu.indiana.p532.rpl.domain.knowledge;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocol_steps")
public class ProtocolStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_protocol_id")
    private Protocol subProtocol;

    // comma-separated step names that this step depends on within its parent protocol
    @Column(name = "depends_on")
    private String dependsOn;

    @Column(name = "step_order")
    private int stepOrder;

    protected ProtocolStep() {}

    public ProtocolStep(String name, Protocol subProtocol, String dependsOn, int stepOrder) {
        this.name = name;
        this.subProtocol = subProtocol;
        this.dependsOn = dependsOn;
        this.stepOrder = stepOrder;
    }

    public Long getId() { return id; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Protocol getSubProtocol() { return subProtocol; }
    public void setSubProtocol(Protocol subProtocol) { this.subProtocol = subProtocol; }
    public String getDependsOn() { return dependsOn; }
    public void setDependsOn(String dependsOn) { this.dependsOn = dependsOn; }
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

    public List<String> getDependsOnList() {
        if (dependsOn == null || dependsOn.isBlank()) return new ArrayList<>();
        return List.of(dependsOn.split(","));
    }
}
