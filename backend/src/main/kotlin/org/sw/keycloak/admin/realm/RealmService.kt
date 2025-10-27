package org.sw.keycloak.admin.realm

import org.keycloak.representations.idm.RealmRepresentation
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.sw.keycloak.KeyCloakConfig
import org.sw.keycloak.admin.AdminService

@Service
class RealmService(
    config: KeyCloakConfig,
    private val resourceLoader: ResourceLoader
) : AdminService(config) {

//    - `GET /admin/realms` - Realm 목록 조회
//    - `POST /admin/realms` - Realm 생성
//    - `GET /admin/realms/{realm}` - 특정 Realm 조회
//    - `PUT /admin/realms/{realm}` - Realm 수정
//    - `DELETE /admin/realms/{realm}` - Realm 삭제

    fun list(): List<RealmRepresentation> {
//        val token: String = accessToken().token
        return super.config.builder()
            .realms().findAll()
    }



}
