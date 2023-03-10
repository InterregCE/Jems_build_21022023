package io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create

data class CreateProjectPartnerReportWorkPackage(
    val workPackageId: Long?,
    val number: Int,

    val activities: List<CreateProjectPartnerReportWorkPackageActivity>,
    val outputs: List<CreateProjectPartnerReportWorkPackageOutput>,
)
