package org.sw.keycloak.user

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 인증 관련 설정
 * application.yml의 auth 섹션을 읽어옴
 */
@Configuration
@ConfigurationProperties(prefix = "auth")
data class AuthConfig(
    var defaultRealm: String = "",
    var defaultClientId: String = "",
    var clientSecret: String? = null,
    var redirectUri: String = "",
    var logoutRedirectUri: String = ""
)
