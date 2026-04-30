package edu.indiana.p532.rpl.controller;

import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.dto.ResourceTypeDto;
import edu.indiana.p532.rpl.manager.ResourceTypeManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    private final ResourceTypeManager resourceTypeManager;

    public ResourceTypeController(ResourceTypeManager resourceTypeManager) {
        this.resourceTypeManager = resourceTypeManager;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return resourceTypeManager.listAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return toMap(resourceTypeManager.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody ResourceTypeDto dto) {
        return toMap(resourceTypeManager.create(dto));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ResourceTypeDto dto) {
        return toMap(resourceTypeManager.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        resourceTypeManager.delete(id);
    }

    private Map<String, Object> toMap(ResourceType rt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rt.getId());
        map.put("name", rt.getName());
        map.put("kind", rt.getKind().name());
        map.put("unitOfMeasure", rt.getUnitOfMeasure());
        map.put("poolAccountId",   rt.getPoolAccount() != null ? rt.getPoolAccount().getId()   : null);
        map.put("poolAccountName", rt.getPoolAccount() != null ? rt.getPoolAccount().getName() : null);
        return map;
    }
}
