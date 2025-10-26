package org.sw.keycloak.global.exception.client

import org.springframework.http.HttpStatus
import org.sw.keycloak.global.exception.CommonException

class NotFoundException private constructor(
    val detail: NotFound,
    message: String? = null,
    cause: Exception? = null
) : CommonException(HttpStatus.NOT_FOUND, message, cause) {

    companion object {
        fun ofPath(message: String? = null, cause: Exception? = null): NotFoundException {
            return NotFoundException(NotFound.NOT_FOUND_PATH, message, cause)
        }
    }


}