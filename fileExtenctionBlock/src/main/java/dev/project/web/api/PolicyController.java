package dev.project.web.api;

import dev.project.domain.policy.PolicyService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy/extensions")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * UI 초기 로딩용: 고정/커스텀 확장자 정책 전체 조회
     */
    @GetMapping
    public ResponseEntity<PolicyService.PolicySnapshot> getPolicy(
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId
    ) {
        return ResponseEntity.ok(policyService.getSnapshot(tenantId));
    }

    /**
     * 고정 확장자 토글
     */
    @PutMapping("/fixed")
    public ResponseEntity<Void> setFixed(
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @RequestBody FixedToggleRequest request
    ) {
        policyService.setFixedBlocked(tenantId, request.ext(), request.blocked());
        return ResponseEntity.noContent().build();
    }

    /**
     * 커스텀 확장자 추가
     */
    @PostMapping("/custom")
    public ResponseEntity<Void> addCustom(
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @RequestBody CustomAddRequest request
    ) {
        policyService.addCustom(tenantId, request.ext());
        return ResponseEntity.noContent().build();
    }

    /**
     * 커스텀 확장자 삭제
     */
    @DeleteMapping("/custom/{ext}")
    public ResponseEntity<Void> removeCustom(
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @PathVariable("ext") String ext
    ) {
        policyService.removeCustom(tenantId, ext);
        return ResponseEntity.noContent().build();
    }

    // ===== Request DTOs =====

    public record FixedToggleRequest(
            @NotBlank String ext,
            boolean blocked
    ) {}

    public record CustomAddRequest(
            @NotBlank String ext
    ) {}
}
