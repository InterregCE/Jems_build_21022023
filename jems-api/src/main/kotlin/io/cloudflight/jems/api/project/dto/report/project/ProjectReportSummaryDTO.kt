package io.cloudflight.jems.api.project.dto.report.project

import io.cloudflight.jems.api.project.dto.ProjectPeriodDTO
import io.cloudflight.jems.api.project.dto.contracting.reporting.ContractingDeadlineTypeDTO
import java.time.LocalDate
import java.time.ZonedDateTime

data class ProjectReportSummaryDTO(
    val id: Long,
    val reportNumber: Int,
    val status: ProjectReportStatusDTO,
    val linkedFormVersion: String,
    val type: ContractingDeadlineTypeDTO?,
    val periodDetail: ProjectPeriodDTO?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val reportingDate: LocalDate?,
    val createdAt: ZonedDateTime,
    val firstSubmission: ZonedDateTime?,
    val verificationDate: ZonedDateTime?,
    val deletable: Boolean,
)
