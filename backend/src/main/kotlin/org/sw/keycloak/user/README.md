# User Authentication API

Keycloak과 연동하여 사용자 인증을 처리하는 API입니다. Spring Security 없이 직접 구현되었습니다.

## 설정 방법

### 1. application.yml 설정

```yaml
# Keycloak 서버 설정
keycloak:
  uri: http://localhost:11011
  realm: master
  client-id: admin-cli
  username: admin
  password: admin

# 사용자 인증 관련 설정
auth:
  default-realm: service  # TODO: 사용할 realm 이름
  default-client-id: my-app-client  # TODO: Keycloak에서 생성한 client-id
  client-secret: your-secret-here  # TODO: Confidential Client인 경우 secret (Public Client면 비워두세요)
  redirect-uri: http://localhost:11010/api/auth/callback  # TODO: 콜백 URI
  logout-redirect-uri: http://localhost:3000  # TODO: 로그아웃 후 리다이렉트 URI
```

### 2. Keycloak Client 설정

Keycloak Admin Console에서 다음과 같이 설정해야 합니다:

**Client 생성:**
- Client ID: `my-app-client` (application.yml의 default-client-id와 동일)
- Client Type: `OpenID Connect`
- Client Authentication: 
  - ON (Confidential) - 백엔드 앱
  - OFF (Public) - SPA/모바일 앱

**Valid Redirect URIs:**
```
http://localhost:11010/api/auth/callback
http://localhost:11010/*
```

**Web Origins:**
```
http://localhost:11010
http://localhost:3000
```

## API 엔드포인트

### 1. 로그인
```http
GET /api/auth/login?realm={realm}&clientId={clientId}
```

**파라미터:**
- `realm` (optional): Keycloak realm (기본값: application.yml의 default-realm)
- `clientId` (optional): Client ID (기본값: application.yml의 default-client-id)

**동작:**
- Keycloak 로그인 페이지로 리다이렉트됩니다
- 사용자가 로그인하면 `/api/auth/callback`으로 돌아옵니다

**예제:**
```bash
# 브라우저에서 접속
http://localhost:11010/api/auth/login

# 특정 realm 지정
http://localhost:11010/api/auth/login?realm=my-realm
```

---

### 2. 콜백 (자동 호출)
```http
GET /api/auth/callback?code={code}&state={state}
```

**파라미터:**
- `code`: Authorization Code (Keycloak이 자동으로 전달)
- `state`: CSRF 방지용 State (Keycloak이 자동으로 전달)

**응답:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cC...",
  "token_type": "Bearer",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "session_state": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scope": "openid email profile"
}
```

**참고:**
- 이 엔드포인트는 Keycloak이 자동으로 호출합니다
- 토큰은 세션에 자동으로 저장됩니다

---

### 3. 현재 사용자 정보 조회
```http
GET /api/auth/me
```

**응답:**
```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "email_verified": true,
  "name": "홍길동",
  "preferred_username": "hong",
  "given_name": "길동",
  "family_name": "홍",
  "email": "hong@example.com"
}
```

**오류:**
- 401 Unauthorized: 로그인하지 않은 경우

**예제:**
```bash
curl http://localhost:11010/api/auth/me \
  -b cookies.txt
```

---

### 4. 로그아웃
```http
POST /api/auth/logout
```

**동작:**
- Keycloak 세션 종료
- 서버 세션 무효화
- 설정된 페이지로 리다이렉트

**예제:**
```bash
curl -X POST http://localhost:11010/api/auth/logout \
  -b cookies.txt
```

---

### 5. 토큰 갱신
```http
POST /api/auth/refresh
```

**응답:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cC...",
  "token_type": "Bearer"
}
```

**오류:**
- 401 Unauthorized: Refresh Token이 없거나 만료된 경우

**예제:**
```bash
curl -X POST http://localhost:11010/api/auth/refresh \
  -b cookies.txt
```

---

### 6. 토큰 검증
```http
GET /api/auth/validate
```

**응답:**
```json
{
  "valid": true,
  "message": "Token is valid"
}
```

**예제:**
```bash
curl http://localhost:11010/api/auth/validate \
  -b cookies.txt
```

---

## 사용 흐름

### 전체 인증 흐름

```
1. 사용자 접속
   ↓
2. GET /api/auth/login
   ↓
3. Keycloak 로그인 페이지로 리다이렉트
   ↓
4. 사용자 로그인
   ↓
5. GET /api/auth/callback?code=xxx (자동)
   ↓
6. 세션에 토큰 저장
   ↓
7. GET /api/auth/me (사용자 정보 조회)
```

### 프론트엔드 예제

#### HTML + JavaScript
```html
<!DOCTYPE html>
<html>
<head>
    <title>My App</title>
</head>
<body>
    <h1>Welcome</h1>
    
    <div id="user-info" style="display: none;">
        <p>이름: <span id="username"></span></p>
        <p>이메일: <span id="email"></span></p>
        <button onclick="logout()">로그아웃</button>
    </div>
    
    <div id="login-section">
        <button onclick="login()">로그인</button>
    </div>
    
    <script>
        // 페이지 로드 시 로그인 상태 확인
        window.onload = async function() {
            try {
                const response = await fetch('/api/auth/me', {
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const user = await response.json();
                    showUserInfo(user);
                } else {
                    showLoginButton();
                }
            } catch (error) {
                showLoginButton();
            }
        };
        
        function login() {
            window.location.href = '/api/auth/login';
        }
        
        async function logout() {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include'
            });
            window.location.reload();
        }
        
        function showUserInfo(user) {
            document.getElementById('user-info').style.display = 'block';
            document.getElementById('login-section').style.display = 'none';
            document.getElementById('username').textContent = user.name || user.preferred_username;
            document.getElementById('email').textContent = user.email;
        }
        
        function showLoginButton() {
            document.getElementById('user-info').style.display = 'none';
            document.getElementById('login-section').style.display = 'block';
        }
    </script>
</body>
</html>
```

#### React 예제
```typescript
import { useEffect, useState } from 'react';

interface UserInfo {
  sub: string;
  name?: string;
  preferred_username?: string;
  email?: string;
}

function App() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:11010/api/auth/me', {
        credentials: 'include'
      });
      
      if (response.ok) {
        const userData = await response.json();
        setUser(userData);
      }
    } catch (error) {
      console.error('Not logged in');
    } finally {
      setLoading(false);
    }
  };

  const login = () => {
    window.location.href = 'http://localhost:11010/api/auth/login';
  };

  const logout = async () => {
    await fetch('http://localhost:11010/api/auth/logout', {
      method: 'POST',
      credentials: 'include'
    });
    setUser(null);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      {user ? (
        <div>
          <h1>Welcome, {user.name || user.preferred_username}!</h1>
          <p>Email: {user.email}</p>
          <button onClick={logout}>로그아웃</button>
        </div>
      ) : (
        <div>
          <h1>Please login</h1>
          <button onClick={login}>로그인</button>
        </div>
      )}
    </div>
  );
}

export default App;
```

---

## 보안 고려사항

### 1. CORS 설정
프론트엔드가 다른 도메인에 있다면 CORS 설정이 필요합니다:

```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true)
    }
}
```

### 2. HTTPS 사용
프로덕션 환경에서는 반드시 HTTPS를 사용하세요:
- Keycloak URI: `https://keycloak.yourdomain.com`
- Redirect URI: `https://app.yourdomain.com/api/auth/callback`

### 3. 세션 보안
```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true  # HTTPS 환경에서
        same-site: lax
```

---

## 트러블슈팅

### 1. "Invalid state parameter" 오류
- **원인**: CSRF 공격 방지 검증 실패
- **해결**: 쿠키가 제대로 전송되는지 확인 (`credentials: 'include'`)

### 2. "Redirect URI mismatch" 오류
- **원인**: Keycloak에 등록된 Redirect URI와 다름
- **해결**: Keycloak Admin Console에서 Redirect URI 확인 및 수정

### 3. 401 Unauthorized
- **원인**: 로그인하지 않았거나 토큰이 만료됨
- **해결**: `/api/auth/login`으로 재로그인 또는 `/api/auth/refresh`로 토큰 갱신

### 4. CORS 오류
- **원인**: 프론트엔드 도메인이 허용되지 않음
- **해결**: 
  1. 백엔드에 CORS 설정 추가
  2. Keycloak Client의 Web Origins 확인

---

## 추가 기능 구현 예제

### 1. 인증 인터셉터 만들기

```kotlin
@Component
class AuthInterceptor : HandlerInterceptor {
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val session = request.session
        val accessToken = session.getAttribute("access_token") as? String
        
        if (accessToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in")
            return false
        }
        
        return true
    }
}

@Configuration
class WebMvcConfig(
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",  // 인증 API는 제외
                "/api/public/**"  // 공개 API 제외
            )
    }
}
```

### 2. 커스텀 어노테이션으로 인증 체크

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireAuth

@Aspect
@Component
class AuthAspect {
    
    @Before("@annotation(RequireAuth)")
    fun checkAuth(joinPoint: JoinPoint) {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val session = request.session
        val accessToken = session.getAttribute("access_token") as? String
        
        if (accessToken == null) {
            throw UnauthorizedException("Not logged in")
        }
    }
}

// 사용 예
@RestController
@RequestMapping("/api/protected")
class ProtectedController {
    
    @GetMapping("/data")
    @RequireAuth  // 이 어노테이션만 추가하면 자동으로 인증 체크
    fun getData(): ResponseEntity<String> {
        return ResponseEntity.ok("Protected data")
    }
}
```

---

## 참고자료

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth 2.0 Authorization Code Flow](https://oauth.net/2/grant-types/authorization-code/)
- [OpenID Connect Specification](https://openid.net/connect/)
