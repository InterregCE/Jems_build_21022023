package io.cloudflight.jems.server.project.repository.report.partner.procurement.attachment

import io.cloudflight.jems.server.project.entity.report.partner.procurement.file.ProjectPartnerReportProcurementFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProjectPartnerReportProcurementAttachmentRepository :
    JpaRepository<ProjectPartnerReportProcurementFileEntity, Long> {

    fun findTop30ByProcurementIdAndCreatedInReportIdLessThanEqualOrderByCreatedInReportIdAscIdAsc(
        procurementId: Long,
        reportId: Long,
    ): List<ProjectPartnerReportProcurementFileEntity>

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.procurement.id = :procurementId AND e.createdInReportId <= :reportId")
    fun countAttachmentsCreatedBeforeIncludingThis(procurementId: Long, reportId: Long): Long

}
