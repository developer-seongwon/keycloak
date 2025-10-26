package org.sw.keycloak.admin.realm

import org.keycloak.admin.client.resource.RealmsResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/keycloak/admin/realms")
class RealmController(
    private val service: RealmService
) : RealmSpec {


    @GetMapping
    fun list(): ResponseEntity<Any> {

        return ResponseEntity.ok()
            .body(this.service.list())

    }

}
