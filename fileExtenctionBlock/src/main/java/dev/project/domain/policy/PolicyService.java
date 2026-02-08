package dev.project.domain.policy;

import dev.project.common.logging.MdcUtil;
import dev.project.domain.policy.excep.BlockedExtension;
import dev.project.domain.policy.excep.DuplicateExtensionException;
import dev.project.domain.policy.excep.ExtensionLimitExceededException;
import dev.project.domain.policy.excep.InvalidExtensionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PolicyService {

    public static final int CUSTOM_LIMIT = 200;

    private final BlockedExtensionRepository repository;

    public PolicyService(BlockedExtensionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PolicySnapshot getSnapshot(String tenantId) {
        List<BlockedExtension> all = repository.findAllByTenantId(tenantId);

        Set<String> blockedSet = all.stream()
                .map(BlockedExtension::getExtension)
                .collect(Collectors.toSet());

        List<FixedItem> fixed = Arrays.stream(FixedExtension.values())
                .map(f -> new FixedItem(f.ext(), blockedSet.contains(f.ext())))
                .toList();

        List<String> custom = all.stream()
                .filter(e -> e.getSourceType() == SourceType.CUSTOM)
                .map(BlockedExtension::getExtension)
                .sorted()
                .toList();

        return new PolicySnapshot(tenantId, fixed, custom, CUSTOM_LIMIT, custom.size());
    }

    /**
     * 고정 확장자 차단 on/off
     */
    public void setFixedBlocked(String tenantId, String rawExt, boolean blocked) {
        String ext = ExtensionNormalizer.normalizeOrThrow(rawExt);

        if (!FixedExtension.isFixed(ext)) {
            throw new InvalidExtensionException(rawExt);
        }

        Optional<BlockedExtension> existing =
                repository.findByTenantIdAndExtension(tenantId, ext);

        if (blocked) {
            if (existing.isPresent()) return;

            repository.save(BlockedExtension.of(tenantId, ext, SourceType.FIXED));

            MdcUtil.withMdc(Map.of(
                    "event", "policy.fixed.block",
                    "tenantId", tenantId,
                    "extension", ext
            ), () -> log.info("fixed extension blocked"));

        } else {
            if (existing.isEmpty()) return;

            repository.delete(existing.get());

            MdcUtil.withMdc(Map.of(
                    "event", "policy.fixed.unblock",
                    "tenantId", tenantId,
                    "extension", ext
            ), () -> log.info("fixed extension unblocked"));
        }
    }

    /**
     * 커스텀 확장자 추가
     */
    public void addCustom(String tenantId, String rawExt) {
        String ext = ExtensionNormalizer.normalizeOrThrow(rawExt);

        if (FixedExtension.isFixed(ext)) {
            throw new DuplicateExtensionException(ext);
        }

        if (repository.existsByTenantIdAndExtension(tenantId, ext)) {
            throw new DuplicateExtensionException(ext);
        }

        long customCount =
                repository.countByTenantIdAndSourceType(tenantId, SourceType.CUSTOM);

        if (customCount >= CUSTOM_LIMIT) {
            throw new ExtensionLimitExceededException(CUSTOM_LIMIT);
        }

        repository.save(BlockedExtension.of(tenantId, ext, SourceType.CUSTOM));

        MdcUtil.withMdc(Map.of(
                "event", "policy.custom.add",
                "tenantId", tenantId,
                "extension", ext
        ), () -> log.info("custom extension added"));
    }

    /**
     * 커스텀 확장자 삭제
     */
    public void removeCustom(String tenantId, String rawExt) {
        String ext = ExtensionNormalizer.normalizeOrThrow(rawExt);

        if (FixedExtension.isFixed(ext)) {
            throw new InvalidExtensionException(rawExt);
        }

        repository.deleteByTenantIdAndExtension(tenantId, ext);

        MdcUtil.withMdc(Map.of(
                "event", "policy.custom.remove",
                "tenantId", tenantId,
                "extension", ext
        ), () -> log.info("custom extension removed"));
    }

    // ===== Service DTOs =====

    public record FixedItem(String ext, boolean blocked) {}

    public record PolicySnapshot(
            String tenantId,
            List<FixedItem> fixed,
            List<String> custom,
            int limit,
            int count
    ) {}
}
