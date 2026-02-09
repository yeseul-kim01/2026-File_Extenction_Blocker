package dev.project.domain.connection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(
        name = "Connection Code",
        description = "크롬 확장과 서버를 연결하기 위한 1회성 연결 코드 발급/검증 API"
)
@RestController
@RequestMapping("/api/connection-codes")
public class ConnectionCodeController {

    private final ConnectionCodeService service;

    public ConnectionCodeController(ConnectionCodeService service) {
        this.service = service;
    }

    @Operation(
            summary = "연결 코드 발급",
            description = """
            특정 Tenant에 대해 **1회성 연결 코드**를 발급합니다.

            - 관리자(Web Console)에서만 사용
            - 발급된 코드는 만료 시간 이전까지 1회만 사용 가능
            - 크롬 확장에서 최초 인증 시 사용됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연결 코드 발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "Tenant 인증 실패")
    })
    @PostMapping
    public ResponseEntity<IssueResponse> issue(
            @RequestHeader("X-Tenant-Id")
            @NotBlank
            @Schema(description = "연결 코드를 발급할 Tenant ID", example = "tenant-1234")
            String tenantId
    ) {
        var r = service.issue(tenantId);
        return ResponseEntity.ok(new IssueResponse(r.code(), r.expiresAt()));
    }

    @Operation(
            summary = "연결 코드 검증 및 소비",
            description = """
            크롬 확장에서 전달한 연결 코드를 검증합니다.

            - 코드가 유효하면 Tenant ID 반환
            - 검증 성공 시 코드는 즉시 **소비(재사용 불가)**
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연결 코드 검증 성공"),
            @ApiResponse(responseCode = "400", description = "코드 만료 또는 잘못된 코드")
    })
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(
            @Valid @RequestBody VerifyRequest req
    ) {
        var r = service.verifyAndConsume(req.code());
        return ResponseEntity.ok(new VerifyResponse(r.tenantId()));
    }

    /* ===== DTO ===== */

    @Schema(description = "연결 코드 발급 응답")
    public record IssueResponse(
            @Schema(description = "발급된 연결 코드", example = "KSB-7F92QX")
            String code,

            @Schema(description = "연결 코드 만료 시각 (UTC)")
            Instant expiresAt
    ) {}

    @Schema(description = "연결 코드 검증 요청")
    public record VerifyRequest(
            @NotBlank
            @Schema(description = "연결 코드", example = "KSB-7F92QX")
            String code
    ) {}

    @Schema(description = "연결 코드 검증 결과")
    public record VerifyResponse(
            @Schema(description = "연결된 Tenant ID", example = "tenant-1234")
            String tenantId
    ) {}
}
