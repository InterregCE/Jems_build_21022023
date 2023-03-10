package io.cloudflight.jems.server.programme.service.indicator.update_result_indicator

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationBadRequestException
import io.cloudflight.jems.server.common.exception.ApplicationException
import io.cloudflight.jems.server.common.exception.ApplicationUnprocessableException

const val UPDATE_RESULT_INDICATOR_ERROR_CODE_PREFIX = "S-IND-UOI"
const val UPDATE_RESULT_INDICATOR_ERROR_KEY_PREFIX = "use.case.update.result.indicator"

class UpdateResultIndicatorException(cause: Throwable) : ApplicationException(
    code = UPDATE_RESULT_INDICATOR_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$UPDATE_RESULT_INDICATOR_ERROR_KEY_PREFIX.failed"), cause = cause
)

class IdentifierIsUsedException : ApplicationBadRequestException(
    code = "$UPDATE_RESULT_INDICATOR_ERROR_CODE_PREFIX-001",
    i18nMessage = I18nMessage("$UPDATE_RESULT_INDICATOR_ERROR_KEY_PREFIX.identifier.is.used"),
    formErrors = mapOf("identifier" to I18nMessage("$UPDATE_RESULT_INDICATOR_ERROR_KEY_PREFIX.identifier.is.used")),
    cause = null
)

class ResultIndicatorCannotBeChangedAfterCallIsPublished(fields: Set<String>): ApplicationUnprocessableException(
    code = "$UPDATE_RESULT_INDICATOR_ERROR_CODE_PREFIX-002",
    i18nMessage = I18nMessage("$UPDATE_RESULT_INDICATOR_ERROR_KEY_PREFIX.indicator.cannot.be.changed.after.first.published.call"),
    formErrors = fields.mapTo(HashSet()) { Pair(it, I18nMessage("forbidden.to.change.this.field.when.call.is.published")) }.toMap(),
)
