# Keycloak Backend API

Keycloak API를 활용한 인증/인가 시스템

## 📋 Keycloak Admin REST API 목록

### 🔐 Authentication & Authorization

#### Token Management
- `POST /realms/{realm}/protocol/openid-connect/token` - 액세스 토큰 발급
- `POST /realms/{realm}/protocol/openid-connect/token/introspect` - 토큰 검증
- `POST /realms/{realm}/protocol/openid-connect/logout` - 로그아웃
- `POST /realms/{realm}/protocol/openid-connect/revoke` - 토큰 폐기

### 👥 User Management

#### User CRUD
- `GET /admin/realms/{realm}/users` - 사용자 목록 조회
- `GET /admin/realms/{realm}/users/{id}` - 특정 사용자 조회
- `POST /admin/realms/{realm}/users` - 사용자 생성
- `PUT /admin/realms/{realm}/users/{id}` - 사용자 수정
- `DELETE /admin/realms/{realm}/users/{id}` - 사용자 삭제

#### User Actions
- `PUT /admin/realms/{realm}/users/{id}/reset-password` - 비밀번호 재설정
- `PUT /admin/realms/{realm}/users/{id}/send-verify-email` - 이메일 인증 발송
- `GET /admin/realms/{realm}/users/{id}/sessions` - 사용자 세션 조회
- `DELETE /admin/realms/{realm}/users/{id}/sessions` - 사용자 세션 삭제

### 🎭 Role Management

#### Realm Roles
- `GET /admin/realms/{realm}/roles` - Realm 롤 목록 조회
- `POST /admin/realms/{realm}/roles` - Realm 롤 생성
- `GET /admin/realms/{realm}/roles/{role-name}` - 특정 롤 조회
- `PUT /admin/realms/{realm}/roles/{role-name}` - 롤 수정
- `DELETE /admin/realms/{realm}/roles/{role-name}` - 롤 삭제

#### User Role Mapping
- `GET /admin/realms/{realm}/users/{id}/role-mappings` - 사용자 롤 매핑 조회
- `POST /admin/realms/{realm}/users/{id}/role-mappings/realm` - Realm 롤 할당
- `DELETE /admin/realms/{realm}/users/{id}/role-mappings/realm` - Realm 롤 제거

### 👨‍👩‍👧‍👦 Group Management

#### Group CRUD
- `GET /admin/realms/{realm}/groups` - 그룹 목록 조회
- `POST /admin/realms/{realm}/groups` - 그룹 생성
- `GET /admin/realms/{realm}/groups/{id}` - 특정 그룹 조회
- `PUT /admin/realms/{realm}/groups/{id}` - 그룹 수정
- `DELETE /admin/realms/{realm}/groups/{id}` - 그룹 삭제

#### Group Members
- `GET /admin/realms/{realm}/groups/{id}/members` - 그룹 멤버 조회
- `PUT /admin/realms/{realm}/users/{user-id}/groups/{group-id}` - 그룹에 사용자 추가
- `DELETE /admin/realms/{realm}/users/{user-id}/groups/{group-id}` - 그룹에서 사용자 제거

### 🎫 Client Management

#### Client CRUD
- `GET /admin/realms/{realm}/clients` - 클라이언트 목록 조회
- `POST /admin/realms/{realm}/clients` - 클라이언트 생성
- `GET /admin/realms/{realm}/clients/{id}` - 특정 클라이언트 조회
- `PUT /admin/realms/{realm}/clients/{id}` - 클라이언트 수정
- `DELETE /admin/realms/{realm}/clients/{id}` - 클라이언트 삭제

#### Client Secrets
- `GET /admin/realms/{realm}/clients/{id}/client-secret` - 클라이언트 시크릿 조회
- `POST /admin/realms/{realm}/clients/{id}/client-secret` - 클라이언트 시크릿 재생성

### 🌍 Realm Management

- `GET /admin/realms` - Realm 목록 조회
- `POST /admin/realms` - Realm 생성
- `GET /admin/realms/{realm}` - 특정 Realm 조회
- `PUT /admin/realms/{realm}` - Realm 수정
- `DELETE /admin/realms/{realm}` - Realm 삭제

### 📊 Events & Audit

- `GET /admin/realms/{realm}/events` - 이벤트 조회
- `GET /admin/realms/{realm}/admin-events` - Admin 이벤트 조회

### 🔧 Identity Providers

- `GET /admin/realms/{realm}/identity-provider/instances` - Identity Provider 목록
- `POST /admin/realms/{realm}/identity-provider/instances` - Identity Provider 생성
- `GET /admin/realms/{realm}/identity-provider/instances/{alias}` - 특정 IDP 조회

---

## 🚀 Quick Start

### 1. Admin 토큰 발급
```bash
curl -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli"
```

### 2. API 호출 예시
```bash
curl -X GET "http://localhost:8080/admin/realms/{realm}/users" \
  -H "Authorization: Bearer {access_token}"
```

---

## 📚 참고 자료

- [Keycloak Admin REST API Documentation](https://www.keycloak.org/docs-api/latest/rest-api/index.html)
- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [Keycloak Authorization Services Guide](https://www.keycloak.org/docs/latest/authorization_services/)

---

## 🏗️ 프로젝트 구조

```
backend/
├── src/main/kotlin/org/sw/keycloak/
│   ├── global/          # 전역 설정 (Exception, Filter 등)
│   └── [기능 패키지들]
└── README.md
```
