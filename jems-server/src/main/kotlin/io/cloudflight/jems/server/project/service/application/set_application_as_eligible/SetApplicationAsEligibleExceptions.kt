package io.cloudflight.jems.server.project.service.application.set_application_as_eligible

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException
import io.cloudflight.jems.server.common.exception.ApplicationUnprocessableException

private const val SET_APPLICATION_AS_ELIGIBLE_ERROR_CODE_PREFIX = "S-PA-SAAE"
private const val SET_APPLICATION_AS_ELIGIBLE_ERROR_KEY_PREFIX = "use.case.set.application.as.eligible"

class SetApplicationAsEligibleException(cause: Throwable) : ApplicationException(
    code = SET_APPLICATION_AS_ELIGIBLE_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$SET_APPLICATION_AS_ELIGIBLE_ERROR_KEY_PREFIX.failed"), cause = cause
)

class EligibilityAssessmentMissing : ApplicationUnprocessableException(
    code = "$SET_APPLICATION_AS_ELIGIBLE_ERROR_CODE_PREFIX-001",
    i18nMessage = I18nMessage("$SET_APPLICATION_AS_ELIGIBLE_ERROR_KEY_PREFIX.missing.eligibility.assessment"),
)
