package org.sw.keycloak.global.exception

import org.springframework.http.HttpStatus

abstract class CommonException protected constructor(
    val status: HttpStatus,
    message: String?,
    cause: Exception?
) : Exception(message, cause) {

}
