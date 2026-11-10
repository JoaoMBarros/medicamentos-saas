package br.com.medicacao.saas.organization;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "organization_audit_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class OrganizationAuditEvent {
    @Id
    @ToString.Include
    private UUID id;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "actor_user_id", nullable = false)
    private String actorUserId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "target_user_id")
    private String targetUserId;
    private String details;
    @Column(nullable = false)
    private Instant createdAt;

    public OrganizationAuditEvent(UUID organizationId, String actorUserId, String eventType,
            String targetUserId, String details) {
        this(organizationId, actorUserId, eventType, targetUserId, details, Clock.systemUTC());
    }

    public OrganizationAuditEvent(UUID organizationId, String actorUserId, String eventType,
            String targetUserId, String details, Clock clock) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.targetUserId = targetUserId;
        this.details = details;
        this.createdAt = Instant.now(clock);
    }
}
