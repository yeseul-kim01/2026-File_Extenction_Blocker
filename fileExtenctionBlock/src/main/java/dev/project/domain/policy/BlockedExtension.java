package dev.project.domain.policy;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "blocked_extension",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_extension", columnNames = {"tenant_id", "extension"})
        },
        indexes = {
                @Index(name = "idx_tenant", columnList = "tenant_id")
        }
)
public class BlockedExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SaaS 테넌트 식별자 (고객/조직)
     */
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    /**
     * 확장자 (소문자, 점 제거) 예: "exe", "sh"
     */
    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 16)
    private SourceType sourceType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BlockedExtension() {
        // JPA
    }

    private BlockedExtension(String tenantId, String extension, SourceType sourceType) {
        this.tenantId = tenantId;
        this.extension = extension;
        this.sourceType = sourceType;
        this.createdAt = Instant.now();
    }

    public static BlockedExtension of(String tenantId, String normalizedExtension, SourceType sourceType) {
        return new BlockedExtension(tenantId, normalizedExtension, sourceType);
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getExtension() {
        return extension;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class InvalidExtensionException extends DomainException {

        public static final String CODE = "POLICY_INVALID_EXTENSION";

        public InvalidExtensionException(String raw) {
            super(
                    CODE,
                    "Invalid extension: '" + raw + "'. Use 1-20 chars [a-z0-9] without dot."
            );
        }
    }
}
