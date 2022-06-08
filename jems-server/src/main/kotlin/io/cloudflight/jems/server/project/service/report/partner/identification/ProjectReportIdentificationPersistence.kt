package io.cloudflight.jems.server.project.service.report.partner.identification

import io.cloudflight.jems.server.project.service.report.model.identification.ProjectPartnerReportIdentification
import io.cloudflight.jems.server.project.service.report.model.identification.ProjectPartnerReportPeriod
import io.cloudflight.jems.server.project.service.report.model.identification.UpdateProjectPartnerReportIdentification
import java.math.BigDecimal
import java.util.Optional

interface ProjectReportIdentificationPersistence {

    fun getPartnerReportIdentification(partnerId: Long, reportId: Long): Optional<ProjectPartnerReportIdentification>

    fun getPreviousSpendingFor(reportIds: Set<Long>): BigDecimal

    fun updatePartnerReportIdentification(
        partnerId: Long,
        reportId: Long,
        data: UpdateProjectPartnerReportIdentification,
    ): ProjectPartnerReportIdentification

    fun updateCurrentReportSpending(
        partnerId: Long,
        reportId: Long,
        currentReport: BigDecimal,
    )

    fun getAvailablePeriods(partnerId: Long, reportId: Long): List<ProjectPartnerReportPeriod>

}
