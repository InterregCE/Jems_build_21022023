package io.cloudflight.jems.server.project.service.report.model.partner

import io.cloudflight.jems.server.project.service.report.model.partner.identification.ProjectPartnerReportPeriod
import java.time.LocalDate
import java.time.ZonedDateTime

data class ProjectPartnerReportSummary(
    val id: Long,
    val reportNumber: Int,
    val status: ReportStatus,
    val version: String,
    val firstSubmission: ZonedDateTime?,
    val createdAt: ZonedDateTime,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val periodDetail: ProjectPartnerReportPeriod?,
    var deletable: Boolean,
)
