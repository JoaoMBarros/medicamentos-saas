package br.com.medicacao.saas.organization;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "organization_memberships",
        uniqueConstraints = @UniqueConstraint(name = "uk_membership_organization_user",
                columnNames = {"organization_id", "user_id"}))
public class OrganizationMembership {

    @Id
    private UUID id;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;
    @Column(nullable = false, length = 255)
    private String username;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MembershipRole role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected OrganizationMembership() {
    }

    public OrganizationMembership(UUID organizationId, String userId, String username,
            MembershipRole role, MembershipStatus status) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public MembershipRole getRole() { return role; }
    public MembershipStatus getStatus() { return status; }
    public void changeRole(MembershipRole role) { this.role = role; }
    public void remove() { this.status = MembershipStatus.REMOVED; }
}
