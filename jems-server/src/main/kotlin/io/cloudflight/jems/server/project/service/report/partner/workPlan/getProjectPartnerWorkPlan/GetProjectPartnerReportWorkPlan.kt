package io.cloudflight.jems.server.project.service.report.partner.workPlan.getProjectPartnerWorkPlan

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanViewPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.ProjectPartnerReportWorkPackage
import io.cloudflight.jems.server.project.service.report.partner.workPlan.ProjectPartnerReportWorkPlanPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProjectPartnerReportWorkPlan(
    private val reportWorkPlanPersistence: ProjectPartnerReportWorkPlanPersistence,
) : GetProjectPartnerReportWorkPlanInteractor {

    @CanViewPartnerReport
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetProjectPartnerReportWorkPlanException::class)
    override fun getForPartner(partnerId: Long, reportId: Long): List<ProjectPartnerReportWorkPackage> =
        reportWorkPlanPersistence.getPartnerReportWorkPlanById(partnerId = partnerId, reportId = reportId)

}
