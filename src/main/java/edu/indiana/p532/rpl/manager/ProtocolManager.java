package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ProtocolStep;
import edu.indiana.p532.rpl.dto.ProtocolDto;
import edu.indiana.p532.rpl.dto.ProtocolStepDto;
import edu.indiana.p532.rpl.exception.ResourceNotFoundException;
import edu.indiana.p532.rpl.repository.ProtocolRepository;
import edu.indiana.p532.rpl.repository.ProtocolStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProtocolManager {

    private final ProtocolRepository protocolRepository;
    private final ProtocolStepRepository stepRepository;

    public ProtocolManager(ProtocolRepository protocolRepository,
                            ProtocolStepRepository stepRepository) {
        this.protocolRepository = protocolRepository;
        this.stepRepository = stepRepository;
    }

    @Transactional(readOnly = true)
    public List<Protocol> listAll() {
        List<Protocol> protocols = protocolRepository.findAll();
        protocols.forEach(this::initSteps);
        return protocols;
    }

    @Transactional(readOnly = true)
    public Protocol getById(Long id) {
        Protocol p = protocolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Protocol not found: " + id));
        initSteps(p);
        return p;
    }

    @Transactional
    public Protocol create(ProtocolDto dto) {
        Protocol protocol = new Protocol(dto.name(), dto.description());
        if (dto.steps() != null) {
            int order = 0;
            for (ProtocolStepDto stepDto : dto.steps()) {
                Protocol subProtocol = stepDto.subProtocolId() != null
                        ? protocolRepository.findById(stepDto.subProtocolId()).orElse(null)
                        : null;
                ProtocolStep step = new ProtocolStep(
                        stepDto.name(), subProtocol, stepDto.dependsOn(), order++);
                protocol.addStep(step);
            }
        }
        Protocol saved = protocolRepository.save(protocol);
        initSteps(saved);   // ← force-load BEFORE transaction closes
        return saved;
    }

    @Transactional
    public Protocol update(Long id, ProtocolDto dto) {
        Protocol protocol = getById(id);
        protocol.setName(dto.name());
        protocol.setDescription(dto.description());
        return protocolRepository.save(protocol);
    }

    @Transactional
    public void delete(Long id) {
        protocolRepository.deleteById(id);
    }

    /**
     * Force-initialises the steps collection and each step's subProtocol proxy
     * while the Hibernate session is still open inside @Transactional.
     * With open-in-view=false these proxies are dead outside the transaction.
     */
    private void initSteps(Protocol p) {
        p.getSteps().forEach(s -> {
            if (s.getSubProtocol() != null) {
                s.getSubProtocol().getId(); // touch proxy to initialise it
            }
        });
    }
}