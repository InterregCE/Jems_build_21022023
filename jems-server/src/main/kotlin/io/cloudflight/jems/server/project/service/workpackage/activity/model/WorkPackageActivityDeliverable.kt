package io.cloudflight.jems.server.project.service.workpackage.activity.model

import io.cloudflight.jems.api.project.dto.InputTranslation

data class WorkPackageActivityDeliverable(
    val deliverableId: Long = 0,
    val deliverableNumber: Int = 0,
    val description : Set<InputTranslation> = emptySet(),
    val period: Int? = null,
)
