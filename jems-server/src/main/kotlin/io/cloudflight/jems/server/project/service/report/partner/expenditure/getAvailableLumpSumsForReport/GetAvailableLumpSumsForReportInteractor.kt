package io.cloudflight.jems.server.project.service.report.partner.expenditure.getAvailableLumpSumsForReport

import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportLumpSum

interface GetAvailableLumpSumsForReportInteractor {

    fun getLumpSums(partnerId: Long, reportId: Long): List<ProjectPartnerReportLumpSum>

}
