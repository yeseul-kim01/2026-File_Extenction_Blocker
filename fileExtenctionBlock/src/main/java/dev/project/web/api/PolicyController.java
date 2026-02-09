package dev.project.web.api;

import dev.project.domain.policy.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(
        name = "Policy Management",
        description = "테넌트별 파일 확장자 차단 정책 관리 API"
)
@RestController
@RequestMapping("/api/policy/extensions")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(
            summary = "확장자 정책 전체 조회",
            description = """
        테넌트별 파일 확장자 차단 정책을 조회합니다.
        - 고정(fixed) 확장자
        - 커스텀(custom) 확장자
        - 정책 개수 제한 정보 포함
        """
    )
    @ApiResponse(responseCode = "200", description = "정책 조회 성공")
    @ApiResponse(responseCode = "401", description = "Tenant 헤더 누락 또는 인증 실패")
    @GetMapping
    public ResponseEntity<PolicyService.PolicySnapshot> getPolicy(
            @Parameter(
                    name = "X-Tenant-Id",
                    description = "테넌트 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "tenant-1"
            )
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId
    ) {
        return ResponseEntity.ok(policyService.getSnapshot(tenantId));
    }

    @Operation(
            summary = "고정 확장자 차단 여부 변경",
            description = """
        사전에 정의된 고정 확장자의 차단 여부를 변경합니다.
        예) exe, bat 등
        """
    )
    @ApiResponse(responseCode = "204", description = "정책 변경 성공")
    @ApiResponse(responseCode = "400", description = "확장자 형식 오류")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 확장자")
    @PutMapping("/fixed")
    public ResponseEntity<Void> setFixed(
            @Parameter(
                    name = "X-Tenant-Id",
                    description = "테넌트 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "tenant-1"
            )
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @Valid @RequestBody FixedToggleRequest request
    ) {
        policyService.setFixedBlocked(tenantId, request.ext(), request.blocked());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/fixed/{ext}")
    public ResponseEntity<Void> patchFixed(
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @PathVariable("ext") String ext,
            @Valid @RequestBody FixedBlockedRequest request
    ) {
        policyService.setFixedBlocked(tenantId, ext, request.blocked());
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "커스텀 확장자 차단 추가",
            description = """
        테넌트별로 사용자 정의 확장자를 차단 목록에 추가합니다.
        - 최대 개수 제한 존재
        - 중복 확장자 추가 불가
        """
    )
    @ApiResponse(responseCode = "204", description = "확장자 추가 성공")
    @ApiResponse(responseCode = "409", description = "중복 확장자 또는 제한 초과")
    @PostMapping("/custom")
    public ResponseEntity<Void> addCustom(
            @Parameter(
                    name = "X-Tenant-Id",
                    description = "테넌트 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "tenant-1"
            )
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @Valid @RequestBody CustomAddRequest request
    ) {
        policyService.addCustom(tenantId, request.ext());
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "커스텀 확장자 차단 삭제",
            description = "등록된 커스텀 확장자를 차단 정책에서 제거합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 확장자")
    @DeleteMapping("/custom/{ext}")
    public ResponseEntity<Void> removeCustom(
            @Parameter(
                    name = "X-Tenant-Id",
                    description = "테넌트 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "tenant-1"
            )
            @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
            @Parameter(
                    description = "삭제할 확장자",
                    example = "ps1"
            )
            @PathVariable("ext") String ext
    ) {
        policyService.removeCustom(tenantId, ext);
        return ResponseEntity.noContent().build();
    }


    public record FixedToggleRequest(
            @Schema(description = "확장자 (점 제외)", example = "exe")
            @NotBlank String ext,

            @Schema(description = "차단 여부", example = "true")
            boolean blocked
    ) {}

    public record CustomAddRequest(
            @Schema(description = "추가할 커스텀 확장자", example = "ps1")
            @NotBlank String ext
    ) {}

    public record FixedBlockedRequest(
            boolean blocked
    ) {}
}
