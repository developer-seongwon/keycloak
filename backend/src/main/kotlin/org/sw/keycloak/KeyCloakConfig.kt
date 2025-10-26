package org.sw.keycloak

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "keycloak")
data class KeyCloakConfig(
    var uri: String = "",
    var realm: String = "",
    var username: String = "",
    var password: String = "",
    var clientId: String = ""
) {

    fun builder(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(uri)
            .realm(realm)
            .username(username)
            .password(password)
            .clientId(clientId)
            .build()
    }
}