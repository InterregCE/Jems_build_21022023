package io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureUnitCostBreakdown

import io.cloudflight.jems.server.currency.repository.CurrencyPersistence
import io.cloudflight.jems.server.project.service.report.ProjectReportPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.unitCost.ExpenditureUnitCostBreakdown
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectReportExpenditurePersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectReportUnitCostPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureBreakdown.fillActualCurrencyRates
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GetReportExpenditureUnitCostBreakdownCalculator(
    private val reportPersistence: ProjectReportPersistence,
    private val reportUnitCostPersistence: ProjectReportUnitCostPersistence,
    private val reportExpenditurePersistence: ProjectReportExpenditurePersistence,
    private val currencyPersistence: CurrencyPersistence,
) {

    @Transactional(readOnly = true)
    fun get(partnerId: Long, reportId: Long): ExpenditureUnitCostBreakdown {
        val report = reportPersistence.getPartnerReportById(partnerId = partnerId, reportId)

        val data = reportUnitCostPersistence.getUnitCost(partnerId = partnerId, reportId = reportId)

        if (!report.status.isClosed()) {
            val currentExpenditures = reportExpenditurePersistence.getPartnerReportExpenditureCosts(partnerId = partnerId, reportId = reportId)
            currentExpenditures.fillActualCurrencyRates(getActualCurrencyRates())
            data.fillInCurrent(current = currentExpenditures.getCurrentForUnitCosts())
        }
        val unitCostLines = data.fillInOverviewFields()

        return ExpenditureUnitCostBreakdown(
            unitCosts = unitCostLines,
            total = unitCostLines.sumUp(),
        )
    }

    private fun getActualCurrencyRates() = with(LocalDate.now()) {
        return@with currencyPersistence.findAllByIdYearAndIdMonth(year = year, month = monthValue)
    }

}