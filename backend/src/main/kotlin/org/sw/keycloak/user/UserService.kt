package org.sw.keycloak.user

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.sw.keycloak.KeyCloakConfig
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class UserService(
    private val keycloakConfig: KeyCloakConfig,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {
    
    private val httpClient = HttpClient.newHttpClient()

    /**
     * 로그인 URL 생성
     */
    fun buildLoginUrl(
        realm: String,
        clientId: String,
        redirectUri: String,
        session: HttpSession
    ): String {
        // CSRF 방지를 위한 State 생성
        val state = UUID.randomUUID().toString()
        session.setAttribute("oauth_state", state)
        session.setAttribute("oauth_realm", realm)
        session.setAttribute("oauth_client_id", clientId)
        
        val params = mapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to "openid email profile",
            "state" to state
        )
        
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
        }
        
        return "${keycloakConfig.uri}/realms/$realm/protocol/openid-connect/auth?$queryString"
    }

    /**
     * Authorization Code를 Access Token으로 교환
     */
    fun exchangeCodeForToken(
        realm: String,
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String
    ): TokenResponse {
        val tokenUrl = "${keycloakConfig.uri}/realms/$realm/protocol/openid-connect/token"
        
        val formData = buildString {
            append("grant_type=authorization_code")
            append("&client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}")
            if (clientSecret != null) {
                append("&client_secret=${URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)}")
            }
            append("&code=${URLEncoder.encode(code, StandardCharsets.UTF_8)}")
            append("&redirect_uri=${URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)}")
        }
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to exchange code for token: ${response.body()}")
        }
        
        return objectMapper.readValue(response.body(), TokenResponse::class.java)
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    fun getUserInfo(realm: String, accessToken: String): UserInfo {
        val userInfoUrl = "${keycloakConfig.uri}/realms/$realm/protocol/openid-connect/userinfo"
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(userInfoUrl))
            .header("Authorization", "Bearer $accessToken")
            .GET()
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw UnauthorizedException("Failed to get user info: ${response.body()}")
        }
        
        return objectMapper.readValue(response.body(), UserInfo::class.java)
    }

    /**
     * Refresh Token으로 새 Access Token 발급
     */
    fun refreshAccessToken(
        realm: String,
        clientId: String,
        clientSecret: String?,
        refreshToken: String
    ): TokenResponse {
        val tokenUrl = "${keycloakConfig.uri}/realms/$realm/protocol/openid-connect/token"
        
        val formData = buildString {
            append("grant_type=refresh_token")
            append("&client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}")
            if (clientSecret != null) {
                append("&client_secret=${URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)}")
            }
            append("&refresh_token=${URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)}")
        }
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw UnauthorizedException("Failed to refresh token: ${response.body()}")
        }
        
        return objectMapper.readValue(response.body(), TokenResponse::class.java)
    }

    /**
     * 로그아웃 (Refresh Token 무효화)
     */
    fun logout(
        realm: String,
        clientId: String,
        clientSecret: String?,
        refreshToken: String
    ) {
        val logoutUrl = "${keycloakConfig.uri}/realms/$realm/protocol/openid-connect/logout"
        
        val formData = buildString {
            append("client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}")
            if (clientSecret != null) {
                append("&client_secret=${URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)}")
            }
            append("&refresh_token=${URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)}")
        }
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(logoutUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() !in 200..299) {
            // 로그아웃 실패해도 세션은 정리되므로 예외를 던지지 않음
            println("Logout request failed: ${response.statusCode()} - ${response.body()}")
        }
    }

    /**
     * Access Token 유효성 검증
     */
    fun validateToken(realm: String, accessToken: String): Boolean {
        return try {
            getUserInfo(realm, accessToken)
            true
        } catch (e: Exception) {
            false
        }
    }
}
