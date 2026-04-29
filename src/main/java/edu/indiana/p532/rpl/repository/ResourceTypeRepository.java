package edu.indiana.p532.rpl.repository;

import edu.indiana.p532.rpl.domain.knowledge.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {
}
