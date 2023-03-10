package io.cloudflight.jems.server.project.service.report.partner.procurement.beneficial.getProjectPartnerReportProcurementBeneficial

import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.procurement.beneficial.ProjectPartnerReportProcurementBeneficialOwner
import io.cloudflight.jems.server.project.service.report.partner.procurement.ProjectPartnerReportProcurementPersistence
import io.cloudflight.jems.server.project.service.report.partner.procurement.beneficial.ProjectPartnerReportProcurementBeneficialPersistence
import io.cloudflight.jems.server.project.service.report.partner.procurement.beneficial.fillThisReportFlag
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProjectPartnerReportProcurementBeneficialService(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val reportProcurementPersistence: ProjectPartnerReportProcurementPersistence,
    private val reportProcurementBeneficialPersistence: ProjectPartnerReportProcurementBeneficialPersistence,
) {

    @Transactional(readOnly = true)
    fun getBeneficialOwner(
        partnerId: Long,
        reportId: Long,
        procurementId: Long
    ): List<ProjectPartnerReportProcurementBeneficialOwner> {
        // we need to fetch those because of security to make sure those connections really exist
        val report = reportPersistence.getPartnerReportById(partnerId, reportId = reportId)
        val procurement = reportProcurementPersistence.getById(partnerId, procurementId = procurementId)

        return reportProcurementBeneficialPersistence
            .getBeneficialOwnersBeforeAndIncludingReportId(procurement.id, reportId = report.id)
            .fillThisReportFlag(currentReportId = reportId)
    }

}
