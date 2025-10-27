package org.sw.keycloak.user

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Keycloak Token Response
 */
data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    
    @JsonProperty("expires_in")
    val expiresIn: Int,
    
    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Int,
    
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    
    @JsonProperty("token_type")
    val tokenType: String,
    
    @JsonProperty("id_token")
    val idToken: String?,
    
    @JsonProperty("not-before-policy")
    val notBeforePolicy: Int? = 0,
    
    @JsonProperty("session_state")
    val sessionState: String?,
    
    @JsonProperty("scope")
    val scope: String?
)

/**
 * Keycloak User Info
 */
data class UserInfo(
    @JsonProperty("sub")
    val sub: String,  // User ID
    
    @JsonProperty("email_verified")
    val emailVerified: Boolean? = false,
    
    @JsonProperty("name")
    val name: String?,
    
    @JsonProperty("preferred_username")
    val preferredUsername: String?,
    
    @JsonProperty("given_name")
    val givenName: String?,
    
    @JsonProperty("family_name")
    val familyName: String?,
    
    @JsonProperty("email")
    val email: String?
)

/**
 * 로그인 요청
 */
data class LoginRequest(
    val realm: String,
    val clientId: String? = null
)

/**
 * 로그아웃 응답
 */
data class LogoutResponse(
    val success: Boolean,
    val message: String
)
