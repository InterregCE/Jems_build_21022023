package io.cloudflight.jems.server.project.service.report.partner.control.overview.getReportControlOverview

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException

private const val GET_REPORT_CONTROL_OVERVIEW_ERROR_CODE_PREFIX = "S-GRCO"
private const val GET_REPORT_CONTROL_OVERVIEW_ERROR_KEY_PREFIX = "use.case.get.report.control.overview"

class GetReportControlOverviewException(cause: Throwable) : ApplicationException(
    code = GET_REPORT_CONTROL_OVERVIEW_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$GET_REPORT_CONTROL_OVERVIEW_ERROR_KEY_PREFIX.failed"),
    cause = cause,
)
