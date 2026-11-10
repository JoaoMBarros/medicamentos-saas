package br.com.medicacao.saas.organization;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrganizationResponse create(@Valid @RequestBody OrganizationRequest request) {
        return service.create(request);
    }

    @GetMapping("/{organizationId}")
    OrganizationResponse get(@PathVariable UUID organizationId) {
        return service.get(organizationId);
    }

    @PostMapping("/{organizationId}/suspend")
    OrganizationResponse suspend(@PathVariable UUID organizationId) {
        return service.suspend(organizationId);
    }

    @PostMapping("/{organizationId}/archive")
    OrganizationResponse archive(@PathVariable UUID organizationId) {
        return service.archive(organizationId);
    }

    @GetMapping("/{organizationId}/memberships")
    MembershipPageResponse memberships(@PathVariable UUID organizationId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listMemberships(organizationId, pageable);
    }

    @PostMapping("/{organizationId}/memberships")
    @ResponseStatus(HttpStatus.CREATED)
    MembershipResponse invite(@PathVariable UUID organizationId,
            @Valid @RequestBody MembershipRequest request) {
        return service.invite(organizationId, request);
    }

    @PatchMapping("/{organizationId}/memberships/{membershipId}/role")
    MembershipResponse role(@PathVariable UUID organizationId, @PathVariable UUID membershipId,
            @Valid @RequestBody RoleRequest request) {
        return service.changeRole(organizationId, membershipId, request);
    }

    @DeleteMapping("/{organizationId}/memberships/{membershipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@PathVariable UUID organizationId, @PathVariable UUID membershipId) {
        service.remove(organizationId, membershipId);
    }
}
