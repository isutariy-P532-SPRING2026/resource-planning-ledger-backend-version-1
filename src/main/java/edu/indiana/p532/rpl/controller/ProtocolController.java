package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.knowledge.Protocol;
import edu.indiana.p532.rpl.domain.knowledge.ProtocolStep;
import edu.indiana.p532.rpl.dto.ProtocolDto;
import edu.indiana.p532.rpl.manager.ProtocolManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolManager protocolManager;

    public ProtocolController(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return protocolManager.listAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return toMap(protocolManager.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody ProtocolDto dto) {
        return toMap(protocolManager.create(dto));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ProtocolDto dto) {
        return toMap(protocolManager.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        protocolManager.delete(id);
    }

    private Map<String, Object> toMap(Protocol p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",          p.getId());
        map.put("name",        p.getName());
        map.put("description", p.getDescription() != null ? p.getDescription() : "");
        map.put("steps",       p.getSteps().stream().map(this::stepToMap).toList());
        return map;
    }

    private Map<String, Object> stepToMap(ProtocolStep s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",              s.getId());
        map.put("name",            s.getName());
        map.put("stepOrder",       s.getStepOrder());
        map.put("subProtocolId",   s.getSubProtocol() != null ? s.getSubProtocol().getId()   : null);
        map.put("subProtocolName", s.getSubProtocol() != null ? s.getSubProtocol().getName() : null);
        map.put("dependsOn",       s.getDependsOn() != null ? s.getDependsOn() : "");
        return map;
    }
}