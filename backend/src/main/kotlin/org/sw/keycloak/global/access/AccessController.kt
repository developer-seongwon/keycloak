package org.sw.keycloak.global.access

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.sw.keycloak.global.exception.client.NotFoundException

@RestController
class AccessController(
    private val service: AccessService
) {


    @RequestMapping("/**")
    fun invalid() {
        //TODO update black-list
        throw NotFoundException.Companion.ofPath();
    }
}