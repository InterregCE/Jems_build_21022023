package io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.investments

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ExpenditureInvestmentBreakdownLine(
    val reportInvestmentId: Long,
    val investmentId: Long,
    val investmentNumber: Int,
    val workPackageNumber: Int,
    val title: Set<InputTranslation>,
    val deactivated: Boolean,
    var totalEligibleBudget: BigDecimal,
    var previouslyReported: BigDecimal,
    var previouslyReportedParked: BigDecimal,
    var currentReport: BigDecimal,
    var currentReportReIncluded: BigDecimal,
    var totalEligibleAfterControl: BigDecimal,
    var totalReportedSoFar: BigDecimal = BigDecimal.ZERO,
    var totalReportedSoFarPercentage: BigDecimal = BigDecimal.ZERO,
    var remainingBudget: BigDecimal = BigDecimal.ZERO,
)
