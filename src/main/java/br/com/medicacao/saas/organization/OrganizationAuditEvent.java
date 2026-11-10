package br.com.medicacao.saas.organization;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "organization_audit_events")
public class OrganizationAuditEvent {
    @Id
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

    protected OrganizationAuditEvent() {
    }

    public OrganizationAuditEvent(UUID organizationId, String actorUserId, String eventType,
            String targetUserId, String details) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.targetUserId = targetUserId;
        this.details = details;
        this.createdAt = Instant.now();
    }
}
