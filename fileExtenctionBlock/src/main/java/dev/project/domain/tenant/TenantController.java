package dev.project.domain.tenant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(
        name = "Tenant",
        description = "크롬 확장 최초 실행 시 Tenant(API Key)를 발급하는 API"
)
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Operation(
            summary = "Tenant(API Key) 발급",
            description = """
            크롬 확장 프로그램이 **최초 실행 시 호출**하여 Tenant(API Key)를 발급받습니다.

            - 웹 UI에서는 사용하지 않음
            - 발급된 Tenant ID는 이후 모든 API 요청의 인증 수단으로 사용
            - GitHub OAuth / SaaS API Key 발급 흐름과 동일한 개념
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant 발급 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/issue")
    public ResponseEntity<TenantService.IssueTenantResult> issue() {
        return ResponseEntity.ok(tenantService.issueTenant());
    }
}
