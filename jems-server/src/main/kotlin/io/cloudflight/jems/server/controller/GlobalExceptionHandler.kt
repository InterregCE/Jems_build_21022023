package io.cloudflight.jems.server.controller

import io.cloudflight.jems.server.exception.DuplicateFileException
import io.cloudflight.jems.server.exception.I18nFieldError
import io.cloudflight.jems.server.exception.I18nValidationError
import io.cloudflight.jems.server.exception.I18nValidationException
import io.cloudflight.jems.server.exception.ResourceNotFoundException
import org.hibernate.exception.ConstraintViolationException
import org.springframework.core.NestedRuntimeException
import org.springframework.expression.ExpressionInvocationTargetException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.IllegalArgumentException


@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(DuplicateFileException::class)
    fun duplicateFileExceptionTransformer(exception: DuplicateFileException): ResponseEntity<DuplicateFileException.DuplicateFileError> {
        return ResponseEntity
            .unprocessableEntity()
            .body(exception.error)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun error404Transformer(exception: ResourceNotFoundException): ResponseEntity<I18nValidationError> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                if (exception.entity != null)
                    I18nValidationError(
                        httpStatus = HttpStatus.NOT_FOUND,
                        i18nKey = "${exception.entity}.not.exists"
                    ) else null
            )
    }

    /**
     * If there is an exception during @PreAuthorize or @PostAuthorize,
     * this exception is wrapped by Spring into IllegalArgumentException.
     *
     * As this exception is important for us, we need to catch it inside the cause.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentExceptionHandler(exception: IllegalArgumentException): ResponseEntity<I18nValidationError> {
        val invTarget = exception.cause
        if (invTarget is ExpressionInvocationTargetException) {
            val cause = invTarget.cause
            if (cause is I18nValidationException)
                return customErrorTransformer(cause)
            if (cause is ResourceNotFoundException)
                return error404Transformer(cause)
        }
        throw exception
    }

    @ExceptionHandler(NestedRuntimeException::class)
    fun nestedRuntimeExceptionTransformer(exception: NestedRuntimeException): ResponseEntity<I18nValidationError> {
        getI18nErrorIfConstraintViolation(exception)?.let { return ResponseEntity.unprocessableEntity().body(it) }
        if (!DB_ERROR_WHITE_LIST.contains(exception.rootCause?.message))
            throw exception

        return ResponseEntity
            .unprocessableEntity()
            .body(
                I18nValidationError(
                    httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                    i18nKey = exception.rootCause?.message
                )
            )
    }

    /**
     * This is fall-back solution for MariaDB unique constraint handling.
     * MariaDB does not support transactions during batch processing, so UNIQUE constraint is not checked before commit,
     * but is checked before every insert/update on each row.
     *
     * This handler provides us with at least a small amount of info why such problem occurred.
     */
    private fun getI18nErrorIfConstraintViolation(exception: NestedRuntimeException): I18nValidationError? {
        val message = exception.rootCause?.message ?: ""
        val cause = exception.cause
        if (message.startsWith("Duplicate entry") && cause is ConstraintViolationException) {
            return I18nValidationError(
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                i18nFieldErrors = mapOf(cause.constraintName to I18nFieldError(
                    i18nKey = "${cause.constraintName}.already.in.use",
                    i18nArguments = """Duplicate entry '(.*?)'.*""".toRegex().find(message)?.groups?.get(1)?.value?.let { listOf(it) }
                ))
            )
        }
        return null
    }

    @ExceptionHandler(I18nValidationException::class)
    fun customErrorTransformer(exception: I18nValidationException): ResponseEntity<I18nValidationError> {
        return ResponseEntity
            .status(exception.httpStatus)
            .body(exception.getData())
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException, headers: HttpHeaders,
        status: HttpStatus, request: WebRequest
    ): ResponseEntity<Any> {
        val result = ex.bindingResult

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                I18nValidationError(
                    httpStatus = HttpStatus.BAD_REQUEST,
                    i18nKey = result.globalError?.defaultMessage,
                    i18nFieldErrors = result.fieldErrors.associateBy(
                        { it.field }, { I18nFieldError(it.defaultMessage) }
                    )
                )
            )
    }
}