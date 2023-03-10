package io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportCoFinancingBreakdown

import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.coFinancing.ExpenditureCoFinancingBreakdown
import io.cloudflight.jems.server.project.service.report.partner.contribution.ProjectPartnerReportContributionPersistence
import io.cloudflight.jems.server.project.service.report.partner.contribution.extractOverview
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportExpenditureCoFinancingPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureBreakdown.GetReportExpenditureCostCategoryCalculatorService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetReportExpenditureCoFinancingBreakdownCalculator(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val reportExpenditureCoFinancingPersistence: ProjectPartnerReportExpenditureCoFinancingPersistence,
    private val reportExpenditureCostCategoryCalculatorService: GetReportExpenditureCostCategoryCalculatorService,
    private val reportContributionPersistence: ProjectPartnerReportContributionPersistence,
) {

    @Transactional(readOnly = true)
    fun get(partnerId: Long, reportId: Long): ExpenditureCoFinancingBreakdown {
        val report = reportPersistence.getPartnerReportById(partnerId = partnerId, reportId)

        val data = reportExpenditureCoFinancingPersistence.getCoFinancing(partnerId = partnerId, reportId = reportId)
        val coFinancing = data.toLinesModel()

        if (!report.status.isClosed()) {
            val expenditureTotal = reportExpenditureCostCategoryCalculatorService
                .getSubmittedOrCalculateCurrent(partnerId = partnerId, reportId = reportId).total
            val contributions = reportContributionPersistence
                .getPartnerReportContribution(partnerId, reportId = reportId).extractOverview()

            val currentValues = getCurrentFrom(
                contributions.generateCoFinCalculationInputData(
                    totalEligibleBudget = expenditureTotal.totalEligibleBudget,
                    currentValueToSplit = expenditureTotal.currentReport,
                    funds = report.identification.coFinancing,
                )
            )

            coFinancing.fillInCurrent(current = currentValues)
        }

        return coFinancing.fillInOverviewFields()
    }

}
