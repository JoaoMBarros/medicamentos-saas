package br.com.medicacao.saas.organization;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

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
    List<MembershipResponse> memberships(@PathVariable UUID organizationId) {
        return service.listMemberships(organizationId);
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
