package br.com.medicacao.saas.organization;

import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrganizationControllerTests {

    private final OrganizationService service = Mockito.mock(OrganizationService.class);
    private final OrganizationController controller = new OrganizationController(service);
    private final UUID organizationId = UUID.randomUUID();
    private final UUID membershipId = UUID.randomUUID();

    @Test
    void delegatesOrganizationAndMembershipOperations() {
        OrganizationRequest organizationRequest = new OrganizationRequest("Acme", "acme");
        MembershipRequest membershipRequest = new MembershipRequest("user", "User", MembershipRole.VIEWER);
        RoleRequest roleRequest = new RoleRequest(MembershipRole.ADMIN);

        controller.create(organizationRequest);
        controller.get(organizationId);
        controller.suspend(organizationId);
        controller.archive(organizationId);
        controller.memberships(organizationId);
        controller.invite(organizationId, membershipRequest);
        controller.role(organizationId, membershipId, roleRequest);
        controller.remove(organizationId, membershipId);

        verify(service).create(organizationRequest);
        verify(service).get(organizationId);
        verify(service).suspend(organizationId);
        verify(service).archive(organizationId);
        verify(service).listMemberships(organizationId);
        verify(service).invite(organizationId, membershipRequest);
        verify(service).changeRole(organizationId, membershipId, roleRequest);
        verify(service).remove(organizationId, membershipId);
    }
}
