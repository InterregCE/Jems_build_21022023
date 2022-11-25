package io.cloudflight.jems.server.project.service.report.partner.financialOverview

import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.investments.ExpenditureInvestmentBreakdownLine
import java.math.BigDecimal

interface ProjectReportInvestmentPersistence {
    fun getInvestments(partnerId: Long, reportId: Long): List<ExpenditureInvestmentBreakdownLine>

    fun getInvestmentsCumulative(reportIds: Set<Long>): Map<Long, BigDecimal>

    fun updateCurrentlyReportedValues(partnerId: Long, reportId: Long, currentlyReported: Map<Long, BigDecimal>)
}