package org.sw.keycloak.admin.realm

import org.keycloak.admin.client.resource.RealmsResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/realms")
class RealmController(
    private val service: RealmService
) : RealmSpec {

    //    - `GET /admin/realms` - Realm 목록 조회
//    - `POST /admin/realms` - Realm 생성
//    - `GET /admin/realms/{realm}` - 특정 Realm 조회
//    - `PUT /admin/realms/{realm}` - Realm 수정
//    - `DELETE /admin/realms/{realm}` - Realm 삭제

    @GetMapping
    fun list(): ResponseEntity<Any> {
        return ResponseEntity.ok()
            .body(this.service.list())
    }
    

}
