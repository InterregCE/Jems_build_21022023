package io.cloudflight.jems.server.project.service.report.partner.expenditure.getAvailableInvestmentsForReport

import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportInvestment

interface GetAvailableInvestmentsForReportInteractor {

    fun getInvestments(partnerId: Long, reportId: Long): List<ProjectPartnerReportInvestment>
}
