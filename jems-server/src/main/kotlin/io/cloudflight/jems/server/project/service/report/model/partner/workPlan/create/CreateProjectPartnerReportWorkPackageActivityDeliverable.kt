package io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create

import io.cloudflight.jems.api.project.dto.InputTranslation

data class CreateProjectPartnerReportWorkPackageActivityDeliverable(
    val deliverableId: Long,
    val number: Int,
    val title: Set<InputTranslation>,
    val deactivated: Boolean
)
