package dev.project.domain.policy;

import dev.project.domain.policy.excep.BlockedExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockedExtensionRepository extends JpaRepository<BlockedExtension, Long> {

    List<BlockedExtension> findAllByTenantId(String tenantId);

    List<BlockedExtension> findAllByTenantIdAndSourceType(String tenantId, SourceType sourceType);

    boolean existsByTenantIdAndExtension(String tenantId, String extension);

    Optional<BlockedExtension> findByTenantIdAndExtension(String tenantId, String extension);

    long countByTenantId(String tenantId);

    long countByTenantIdAndSourceType(String tenantId, SourceType sourceType);

    void deleteByTenantIdAndExtension(String tenantId, String extension);
}
