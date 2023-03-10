package io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.lumpSum

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ExpenditureLumpSumBreakdownLine(
    val reportLumpSumId: Long,
    val lumpSumId: Long,
    val name: Set<InputTranslation>,
    val period: Int?,

    var totalEligibleBudget: BigDecimal,
    var previouslyReported: BigDecimal,
    var previouslyReportedParked: BigDecimal,
    var previouslyPaid: BigDecimal,
    var currentReport: BigDecimal,
    var currentReportReIncluded: BigDecimal,
    var totalEligibleAfterControl: BigDecimal,
    var totalReportedSoFar: BigDecimal = BigDecimal.ZERO,
    var totalReportedSoFarPercentage: BigDecimal = BigDecimal.ZERO,
    var remainingBudget: BigDecimal = BigDecimal.ZERO,
)
