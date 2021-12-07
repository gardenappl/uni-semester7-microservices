package ua.knu.carrental.users.service;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.knu.carrental.users.model.User;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Service
public class KeycloakService {
    private static final String USER_ROLE = "user";
    private static final String ADMIN_ROLE = "admin";

    @Value("${keycloak-admin-client.username}")
    private String adminUsername;

    @Value("${keycloak-admin-client.password}")
    private String adminPassword;

    @Value("${keycloak-admin-client.realm}")
    private String adminRealm;

    @Value("${keycloak.authServerUrl}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String usersRealm;

    @Value("${keycloak.resource}")
    private String clientId;

    private Keycloak getKeycloakFor(String username, String password, String realm) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }

//    public long getUserId(KeycloakAuthenticationToken token) {
//        return Long.parseLong(String.valueOf(
//                token.getAccount().getKeycloakSecurityContext().getToken().getOtherClaims().get("passport_id")
//        ));
//    }

    /**
     * @return Keycloak token.
     */
    public String login(String username, String password) {
        return getKeycloakFor(username, password, usersRealm).tokenManager().getAccessToken().getToken();
    }

    /**
     * @return Keycloak user ID.
     */
    public String register(User user, String password) {
        RealmResource realm = getKeycloakFor(adminUsername, adminPassword, adminRealm).realm(usersRealm);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(user.getName());
        String passportId = Long.toString(user.getPassportId());
        userRepresentation.setAttributes(Collections.singletonMap("passport_id", Collections.singletonList(passportId)));

        UsersResource users = realm.users();

        Response response = users.create(userRepresentation);
        switch (response.getStatus()) {
            case HttpServletResponse.SC_CREATED:
                break;
            case HttpServletResponse.SC_CONFLICT:
                throw new RuntimeException(String.format("User %s already exists", user.getName()));
            default:
                throw new RuntimeException(String.format("Could not create Keycloak user: error %d, %s",
                        response.getStatus(), response.getStatusInfo()));
        }

        String userId = CreatedResponseUtil.getCreatedId(response);
        UserResource userResource = users.get(userId);

        //Set password
        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setType(CredentialRepresentation.PASSWORD);
        passwordCredential.setValue(password);
        passwordCredential.setTemporary(false);

        userResource.resetPassword(passwordCredential);

        //Set user role
        RoleRepresentation roleRepresentation = realm.roles().get(USER_ROLE).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));

        return userId;
    }
}
