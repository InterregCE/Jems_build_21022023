package io.cloudflight.jems.server.project.service.report.partner

import io.cloudflight.jems.server.project.service.report.model.partner.base.create.ProjectPartnerReportCreate
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReportSummary

interface ProjectPartnerReportCreatePersistence {

    fun createPartnerReport(report: ProjectPartnerReportCreate): ProjectPartnerReportSummary

}
