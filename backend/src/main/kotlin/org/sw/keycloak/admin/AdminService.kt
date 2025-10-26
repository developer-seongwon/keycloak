package org.sw.keycloak.admin

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.AccessToken
import org.keycloak.representations.AccessTokenResponse
import org.springframework.stereotype.Service
import org.sw.keycloak.KeyCloakConfig

@Service
open class AdminService(
    protected val config: KeyCloakConfig
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(AdminService::class.java)
    }


    fun accessToken(): AccessTokenResponse {
        return this.config.builder()
            .tokenManager()
            .accessToken
            .also { logger.info("{}", it) }
    }
}


