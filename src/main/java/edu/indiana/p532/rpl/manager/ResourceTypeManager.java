package edu.indiana.p532.rpl.manager;

import edu.indiana.p532.rpl.domain.AccountKind;
import edu.indiana.p532.rpl.domain.operational.Account;
import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import edu.indiana.p532.rpl.dto.ResourceTypeDto;
import edu.indiana.p532.rpl.exception.ResourceNotFoundException;
import edu.indiana.p532.rpl.repository.AccountRepository;
import edu.indiana.p532.rpl.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceTypeManager {

    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;

    public ResourceTypeManager(ResourceTypeRepository resourceTypeRepository,
                                AccountRepository accountRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceType> listAll() {
        List<ResourceType> types = resourceTypeRepository.findAll();
        // Touch pool-account name while the session is still open.
        // @OneToOne is EAGER by default, but Hibernate can still return an
        // uninitialised proxy in some configurations; getName() forces a full load.
        types.forEach(rt -> {
            if (rt.getPoolAccount() != null) rt.getPoolAccount().getName();
        });
        return types;
    }

    @Transactional(readOnly = true)
    public ResourceType getById(Long id) {
        ResourceType rt = resourceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceType not found: " + id));
        if (rt.getPoolAccount() != null) rt.getPoolAccount().getName();
        return rt;
    }

    @Transactional
    public ResourceType create(ResourceTypeDto dto) {
        ResourceType rt = new ResourceType(dto.name(), dto.kind(), dto.unitOfMeasure());
        // auto-create linked pool account
        Account poolAccount = accountRepository.save(
                new Account("POOL-" + dto.name(), AccountKind.POOL, null));
        rt.setPoolAccount(poolAccount);
        ResourceType saved = resourceTypeRepository.save(rt);
        // backfill resource type id into pool account for reporting
        poolAccount.setResourceTypeId(saved.getId());
        accountRepository.save(poolAccount);
        return saved;
    }

    @Transactional
    public ResourceType update(Long id, ResourceTypeDto dto) {
        ResourceType rt = getById(id);
        rt.setName(dto.name());
        rt.setKind(dto.kind());
        rt.setUnitOfMeasure(dto.unitOfMeasure());
        return resourceTypeRepository.save(rt);
    }

    @Transactional
    public void delete(Long id) {
        resourceTypeRepository.deleteById(id);
    }
}
