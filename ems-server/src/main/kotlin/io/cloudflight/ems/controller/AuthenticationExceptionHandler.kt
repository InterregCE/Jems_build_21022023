package io.cloudflight.ems.controller

import io.cloudflight.ems.exception.I18nValidationError
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthenticationExceptionHandler : GlobalExceptionHandler() {

    @ExceptionHandler(AuthenticationException::class)
    fun authenticationTransformer(exception: AuthenticationException): ResponseEntity<Any?> {
        return customErrorTransformer(
            I18nValidationError(
                i18nKey = "authentication.failed",
                httpStatus = HttpStatus.UNAUTHORIZED
            )
        )
    }
}