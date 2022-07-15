package io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportCoFinancingBreakdown

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException

private const val GET_REPORT_EXPENDITURE_BREAKDOWN_CO_FINANCING_ERROR_CODE_PREFIX = "S-GREBCF"
private const val GET_REPORT_EXPENDITURE_BREAKDOWN_CO_FINANCING_ERROR_KEY_PREFIX = "use.case.get.report.expenditure.breakdown.co.financing"

class GetReportExpenditureCoFinancingBreakdownException(cause: Throwable) : ApplicationException(
    code = GET_REPORT_EXPENDITURE_BREAKDOWN_CO_FINANCING_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$GET_REPORT_EXPENDITURE_BREAKDOWN_CO_FINANCING_ERROR_KEY_PREFIX.failed"),
    cause = cause
)
