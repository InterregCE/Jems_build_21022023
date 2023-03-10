package io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.unitCost

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ExpenditureUnitCostBreakdownLine(
    val reportUnitCostId: Long,
    val unitCostId: Long,
    val name: Set<InputTranslation>,

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
