package io.cloudflight.jems.server.payments.service.regular.attachment.uploadPaymentAttachment

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException
import io.cloudflight.jems.server.common.exception.ApplicationNotFoundException
import io.cloudflight.jems.server.common.exception.ApplicationUnprocessableException

private const val UPLOAD_PAYMENT_ATTACHMENT_ERROR_CODE_PREFIX = "S-UPA"
private const val UPLOAD_PAYMENT_ATTACHMENT_ERROR_KEY_PREFIX = "use.case.upload.payment.attachment"

class UploadPaymentAttachmentException(cause: Throwable) : ApplicationException(
    code = UPLOAD_PAYMENT_ATTACHMENT_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$UPLOAD_PAYMENT_ATTACHMENT_ERROR_KEY_PREFIX.failed"),
    cause = cause,
)

class FileAlreadyExists : ApplicationUnprocessableException(
    code = "$UPLOAD_PAYMENT_ATTACHMENT_ERROR_CODE_PREFIX-002",
    i18nMessage = I18nMessage("$UPLOAD_PAYMENT_ATTACHMENT_ERROR_KEY_PREFIX.file.already.exists"),
)

class FileTypeNotSupported : ApplicationUnprocessableException(
    code = "$UPLOAD_PAYMENT_ATTACHMENT_ERROR_CODE_PREFIX-003",
    i18nMessage = I18nMessage("$UPLOAD_PAYMENT_ATTACHMENT_ERROR_KEY_PREFIX.type.not.supported")
)
