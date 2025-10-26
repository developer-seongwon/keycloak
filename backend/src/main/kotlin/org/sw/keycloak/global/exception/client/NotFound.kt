package org.sw.keycloak.global.exception.client

import java.net.URI

enum class NotFound(
    val type: URI,
    val title: String
) {
    NOT_FOUND_PATH(URI.create("http://127.0.0.1"), "Not Found Path"),
    NOT_FOUND_USER(URI.create("http://127.0.0.1"), "Not Found User")
}