package io.cloudflight.jems.server.project.service.report.partner.control.overview.getReportControlWorkOverview

import io.cloudflight.jems.server.project.service.budget.calculator.calculateBudget
import io.cloudflight.jems.server.project.service.budget.model.BudgetCostsCalculationResultFull
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerBudgetOptions
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.control.ProjectPartnerReportExpenditureVerification
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureBreakdown.getCategory
import java.math.BigDecimal

fun Collection<ProjectPartnerReportExpenditureVerification>.calculateCertified(
    options: ProjectPartnerBudgetOptions
): BudgetCostsCalculationResultFull {
    val sums = groupBy { it.getCategory() }
        .mapValues { it.value.sumOf { it.certifiedAmount } }
    return calculateBudget(options, sums)
}

fun Collection<ProjectPartnerReportExpenditureVerification>.calculateCertifiedForParked(
    options: ProjectPartnerBudgetOptions
): BudgetCostsCalculationResultFull {
    val sums = groupBy { it.getCategory() }
        .mapValues { it.value.sumOf { it.declaredAmountAfterSubmission ?: BigDecimal.ZERO } }
    return calculateBudget(options, sums)
}

fun Collection<ProjectPartnerReportExpenditureVerification>.onlyParkedOnes() =
    filter { it.parked }
