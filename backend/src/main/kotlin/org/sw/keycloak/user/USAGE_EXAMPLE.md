# 빠른 시작 가이드

## 1단계: application.yml 설정

```yaml
auth:
  default-realm: service  # TODO: Keycloak에서 생성한 realm 이름
  default-client-id: example  # TODO: Keycloak에서 생성한 client-id
  client-secret: null  # TODO: Public Client면 null, Confidential이면 secret 입력
  redirect-uri: http://localhost:11010/api/auth/callback
  logout-redirect-uri: http://localhost:3000
```

## 2단계: Keycloak Client 생성

Keycloak Admin Console (`http://localhost:11011`)에 접속하여:

1. **Realm 선택**: `service` realm으로 이동
2. **Clients** 메뉴 클릭
3. **Create Client** 버튼 클릭
4. 다음 정보 입력:
   - Client ID: `example`
   - Client Type: `OpenID Connect`
5. **Next** 클릭
6. Capability config:
   - Client authentication: `OFF` (Public Client)
   - Standard flow: `ON`
   - Direct access grants: `OFF`
7. **Next** 클릭
8. Login settings:
   - Valid Redirect URIs: 
     ```
     http://localhost:11010/api/auth/callback
     http://localhost:11010/*
     ```
   - Web Origins:
     ```
     http://localhost:11010
     http://localhost:3000
     ```
9. **Save** 클릭

## 3단계: 테스트 사용자 생성

1. **Users** 메뉴 클릭
2. **Add user** 버튼 클릭
3. 정보 입력:
   - Username: `testuser`
   - Email: `test@example.com`
   - First name: `Test`
   - Last name: `User`
   - Email verified: `ON`
4. **Create** 클릭
5. **Credentials** 탭으로 이동
6. **Set password** 클릭:
   - Password: `test1234`
   - Password confirmation: `test1234`
   - Temporary: `OFF`
7. **Save** 클릭

## 4단계: 애플리케이션 실행

```bash
cd backend
./gradlew bootRun
```

## 5단계: 테스트

### 방법 1: 브라우저로 테스트

1. 브라우저 열기
2. `http://localhost:11010/api/auth/login` 접속
3. Keycloak 로그인 페이지가 나타남
4. Username: `testuser`, Password: `test1234` 입력
5. 로그인 성공!
6. `http://localhost:11010/api/auth/me` 접속하여 사용자 정보 확인

### 방법 2: curl로 테스트

```bash
# 1. 로그인 URL 확인 (브라우저에서 진행해야 함)
echo "브라우저에서 http://localhost:11010/api/auth/login 접속"

# 2. 로그인 후 사용자 정보 조회
curl http://localhost:11010/api/auth/me \
  -b cookies.txt \
  -c cookies.txt

# 3. 토큰 검증
curl http://localhost:11010/api/auth/validate \
  -b cookies.txt

# 4. 로그아웃
curl -X POST http://localhost:11010/api/auth/logout \
  -b cookies.txt
```

### 방법 3: Postman으로 테스트

1. **GET** `http://localhost:11010/api/auth/login`
   - 응답 헤더의 Location 복사
   - 브라우저에서 해당 URL로 접속하여 로그인
   
2. **GET** `http://localhost:11010/api/auth/me`
   - 사용자 정보 확인
   
3. **POST** `http://localhost:11010/api/auth/refresh`
   - 토큰 갱신 테스트
   
4. **POST** `http://localhost:11010/api/auth/logout`
   - 로그아웃 테스트

## 6단계: 프론트엔드 연동

### 간단한 HTML 페이지

`frontend/index.html` 파일 생성:

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Keycloak 인증 테스트</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .container {
            border: 1px solid #ddd;
            padding: 20px;
            border-radius: 8px;
        }
        button {
            padding: 10px 20px;
            margin: 5px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            background-color: #007bff;
            color: white;
        }
        button:hover {
            background-color: #0056b3;
        }
        .info {
            background-color: #f8f9fa;
            padding: 15px;
            margin-top: 20px;
            border-radius: 4px;
        }
        .error {
            color: red;
        }
        .success {
            color: green;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Keycloak 인증 테스트</h1>
        
        <div id="loginSection">
            <button onclick="login()">로그인</button>
        </div>
        
        <div id="userSection" style="display: none;">
            <h2>환영합니다!</h2>
            <div class="info" id="userInfo"></div>
            <button onclick="refreshToken()">토큰 갱신</button>
            <button onclick="validateToken()">토큰 검증</button>
            <button onclick="logout()">로그아웃</button>
        </div>
        
        <div id="message" style="margin-top: 20px;"></div>
    </div>
    
    <script>
        const API_BASE = 'http://localhost:11010/api/auth';
        
        // 페이지 로드 시 로그인 상태 확인
        window.onload = checkLoginStatus;
        
        async function checkLoginStatus() {
            try {
                const response = await fetch(`${API_BASE}/me`, {
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const user = await response.json();
                    showUser(user);
                } else {
                    showLogin();
                }
            } catch (error) {
                showLogin();
            }
        }
        
        function login() {
            window.location.href = `${API_BASE}/login`;
        }
        
        async function logout() {
            try {
                await fetch(`${API_BASE}/logout`, {
                    method: 'POST',
                    credentials: 'include'
                });
                showMessage('로그아웃되었습니다.', 'success');
                setTimeout(() => window.location.reload(), 1000);
            } catch (error) {
                showMessage('로그아웃 실패: ' + error.message, 'error');
            }
        }
        
        async function refreshToken() {
            try {
                const response = await fetch(`${API_BASE}/refresh`, {
                    method: 'POST',
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const data = await response.json();
                    showMessage('토큰이 갱신되었습니다.', 'success');
                    console.log('New token:', data);
                } else {
                    showMessage('토큰 갱신 실패', 'error');
                }
            } catch (error) {
                showMessage('토큰 갱신 오류: ' + error.message, 'error');
            }
        }
        
        async function validateToken() {
            try {
                const response = await fetch(`${API_BASE}/validate`, {
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const data = await response.json();
                    showMessage(
                        data.valid ? '토큰이 유효합니다.' : '토큰이 만료되었습니다.',
                        data.valid ? 'success' : 'error'
                    );
                }
            } catch (error) {
                showMessage('토큰 검증 오류: ' + error.message, 'error');
            }
        }
        
        function showUser(user) {
            document.getElementById('loginSection').style.display = 'none';
            document.getElementById('userSection').style.display = 'block';
            document.getElementById('userInfo').innerHTML = `
                <p><strong>사용자 ID:</strong> ${user.sub}</p>
                <p><strong>이름:</strong> ${user.name || user.preferred_username}</p>
                <p><strong>이메일:</strong> ${user.email}</p>
                <p><strong>이메일 인증:</strong> ${user.email_verified ? '✓' : '✗'}</p>
            `;
        }
        
        function showLogin() {
            document.getElementById('loginSection').style.display = 'block';
            document.getElementById('userSection').style.display = 'none';
        }
        
        function showMessage(message, type) {
            const messageDiv = document.getElementById('message');
            messageDiv.innerHTML = `<p class="${type}">${message}</p>`;
            setTimeout(() => messageDiv.innerHTML = '', 3000);
        }
    </script>
</body>
</html>
```

파일을 브라우저로 열기:
```bash
# 간단한 HTTP 서버 실행 (Python)
cd frontend
python3 -m http.server 3000

# 또는 Node.js
npx serve -p 3000
```

브라우저에서 `http://localhost:3000` 접속!

## 트러블슈팅

### 문제 1: "CORS 오류"
**증상:** 브라우저 콘솔에 CORS 관련 에러

**해결:**
```kotlin
// WebConfig.kt 생성
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

### 문제 2: "Redirect URI mismatch"
**증상:** Keycloak에서 리다이렉트 오류

**해결:**
1. Keycloak Admin Console 접속
2. Clients → example → Settings
3. Valid Redirect URIs 확인:
   ```
   http://localhost:11010/api/auth/callback
   http://localhost:11010/*
   ```

### 문제 3: "Invalid state parameter"
**증상:** 콜백 처리 중 에러

**해결:**
- 쿠키가 제대로 설정되는지 확인
- 브라우저 개발자 도구에서 cookies 확인
- `credentials: 'include'` 옵션 확인

### 문제 4: 로그인 페이지가 안 나옴
**증상:** Keycloak 로그인 페이지가 표시되지 않음

**해결:**
1. Keycloak 서버 실행 확인:
   ```bash
   docker ps  # Keycloak 컨테이너 확인
   ```
2. Keycloak URL 확인:
   ```bash
   curl http://localhost:11011
   ```
3. application.yml의 `keycloak.uri` 확인

## 완료! 🎉

이제 Keycloak 인증이 정상적으로 작동합니다.

추가 질문이 있으면 README.md를 참고하세요!
