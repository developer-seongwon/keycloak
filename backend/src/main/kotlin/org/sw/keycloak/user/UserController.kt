package org.sw.keycloak.user

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/auth")
class UserController(
    private val userService: UserService,
    private val authConfig: AuthConfig
) {

    /**
     * 로그인 페이지로 리다이렉트
     * GET /api/auth/login?realm={realm}
     */
    @GetMapping("/login")
    fun login(
        @RequestParam(required = false) realm: String?,
        @RequestParam(required = false) clientId: String?,
        session: HttpSession,
        response: HttpServletResponse
    ) {
        val loginUrl = userService.buildLoginUrl(
            realm = realm ?: authConfig.defaultRealm,
            clientId = clientId ?: authConfig.defaultClientId,
            redirectUri = authConfig.redirectUri,
            session = session
        )
        
        response.sendRedirect(loginUrl)
    }

    /**
     * Keycloak 로그인 후 콜백 처리
     * GET /api/auth/callback?code={code}&state={state}
     */
    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam(required = false) realm: String?,
        session: HttpSession,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        // State 검증
        val savedState = session.getAttribute("oauth_state") as? String
        if (savedState == null || savedState != state) {
            throw SecurityException("Invalid state parameter")
        }
        
        // Realm 정보 복원
        val savedRealm = session.getAttribute("oauth_realm") as? String
            ?: realm
            ?: authConfig.defaultRealm
        
        val savedClientId = session.getAttribute("oauth_client_id") as? String
            ?: authConfig.defaultClientId
        
        // Authorization Code를 Token으로 교환
        val tokenResponse = userService.exchangeCodeForToken(
            realm = savedRealm,
            clientId = savedClientId,
            clientSecret = authConfig.clientSecret,
            code = code,
            redirectUri = authConfig.redirectUri
        )
        
        // 세션에 토큰 저장
        session.setAttribute("access_token", tokenResponse.accessToken)
        session.setAttribute("refresh_token", tokenResponse.refreshToken)
        session.setAttribute("realm", savedRealm)
        
        // State 정리
        session.removeAttribute("oauth_state")
        session.removeAttribute("oauth_realm")
        session.removeAttribute("oauth_client_id")
        
        return ResponseEntity.ok(tokenResponse)
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    fun getCurrentUser(session: HttpSession): ResponseEntity<UserInfo> {
        val accessToken = session.getAttribute("access_token") as? String
            ?: throw UnauthorizedException("Not logged in")
        
        val realm = session.getAttribute("realm") as? String
            ?: authConfig.defaultRealm
        
        val userInfo = userService.getUserInfo(realm, accessToken)
        
        return ResponseEntity.ok(userInfo)
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    fun logout(
        session: HttpSession,
        response: HttpServletResponse
    ) {
        val accessToken = session.getAttribute("access_token") as? String
        val refreshToken = session.getAttribute("refresh_token") as? String
        val realm = session.getAttribute("realm") as? String
            ?: authConfig.defaultRealm
        
        // Keycloak에 로그아웃 요청
        if (refreshToken != null) {
            userService.logout(
                realm = realm,
                clientId = authConfig.defaultClientId,
                clientSecret = authConfig.clientSecret,
                refreshToken = refreshToken
            )
        }
        
        // 세션 무효화
        session.invalidate()
        
        // 로그아웃 후 리다이렉트
        response.sendRedirect(authConfig.logoutRedirectUri)
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(session: HttpSession): ResponseEntity<TokenResponse> {
        val refreshToken = session.getAttribute("refresh_token") as? String
            ?: throw UnauthorizedException("No refresh token")
        
        val realm = session.getAttribute("realm") as? String
            ?: authConfig.defaultRealm
        
        val tokenResponse = userService.refreshAccessToken(
            realm = realm,
            clientId = authConfig.defaultClientId,
            clientSecret = authConfig.clientSecret,
            refreshToken = refreshToken
        )
        
        // 새 토큰으로 세션 업데이트
        session.setAttribute("access_token", tokenResponse.accessToken)
        if (tokenResponse.refreshToken != null) {
            session.setAttribute("refresh_token", tokenResponse.refreshToken)
        }
        
        return ResponseEntity.ok(tokenResponse)
    }

    /**
     * 토큰 검증
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    fun validateToken(session: HttpSession): ResponseEntity<Map<String, Any>> {
        val accessToken = session.getAttribute("access_token") as? String
            ?: return ResponseEntity.ok(mapOf("valid" to false, "message" to "No token"))
        
        val realm = session.getAttribute("realm") as? String
            ?: authConfig.defaultRealm
        
        val isValid = userService.validateToken(realm, accessToken)
        
        return ResponseEntity.ok(mapOf(
            "valid" to isValid,
            "message" to if (isValid) "Token is valid" else "Token is invalid or expired"
        ))
    }
}

/**
 * 인증되지 않은 접근 예외
 */
class UnauthorizedException(message: String) : RuntimeException(message)
