package org.sw.keycloak.global

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.sw.keycloak.global.exception.client.NotFoundException
import org.sw.keycloak.user.UnauthorizedException
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(servlet: HttpServletRequest, exception: NotFoundException): ResponseEntity<ProblemDetail> {
        return ProblemDetail.forStatusAndDetail(exception.status, exception.message)
            .also{
                it.type = exception.detail.type
                it.title = exception.detail.title
                it.instance = servlet.requestURI?.let (URI::create)
            }
            .let { ResponseEntity.status(it.status)
                .header("Content-Type", "application/problem+json")
                .body(it) }
            .also { logger.info("{}", it) }
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(servlet: HttpServletRequest, exception: UnauthorizedException): ResponseEntity<ProblemDetail> {
        return ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatus.UNAUTHORIZED, exception.message ?: "Unauthorized")
            .also {
                it.type = URI.create("about:blank")
                it.title = "Unauthorized"
                it.instance = servlet.requestURI?.let(URI::create)
            }
            .let {
                ResponseEntity.status(it.status)
                    .header("Content-Type", "application/problem+json")
                    .body(it)
            }
            .also { logger.warn("Unauthorized access: {}", exception.message) }
    }
}