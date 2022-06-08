package io.cloudflight.jems.server.project.repository.report.identification

import io.cloudflight.jems.server.project.entity.report.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.identification.ProjectPartnerReportIdentificationEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional

@Repository
interface ProjectPartnerReportIdentificationRepository :
    JpaRepository<ProjectPartnerReportIdentificationEntity, ProjectPartnerReportEntity> {

    @EntityGraph(value = "ProjectPartnerReportIdentificationEntity.withTranslations")
    fun findByReportEntityIdAndReportEntityPartnerId(
        reportId: Long,
        partnerId: Long,
    ): Optional<ProjectPartnerReportIdentificationEntity>

    @Query("SELECT COALESCE(SUM(e.spendingProfile.currentReport), 0) FROM #{#entityName} e WHERE e.reportId IN :reportIds")
    fun sumCurrentlyReportedFor(reportIds: Set<Long>): BigDecimal

}
