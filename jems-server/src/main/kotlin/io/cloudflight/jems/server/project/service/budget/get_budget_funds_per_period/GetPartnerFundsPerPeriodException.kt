package io.cloudflight.jems.server.project.service.budget.get_budget_funds_per_period

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException

private const val GET_PARTNER_FUNDS_PERIOD_ERROR_CODE_PREFIX = "S-GPFPP"
private const val GET_PARTNER_FUNDS_PERIOD_ERROR_KEY_PREFIX = "use.case.get.partner.funds.per.period"

class GetPartnerFundsPerPeriodException(cause: Throwable) : ApplicationException(
    code = GET_PARTNER_FUNDS_PERIOD_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$GET_PARTNER_FUNDS_PERIOD_ERROR_KEY_PREFIX.failed"),
    cause = cause
)
