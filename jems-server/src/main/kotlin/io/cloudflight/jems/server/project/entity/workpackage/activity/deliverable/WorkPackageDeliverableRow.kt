package io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable

import io.cloudflight.jems.server.common.entity.TranslationView

interface WorkPackageDeliverableRow: TranslationView {
    val id: Long
    val deliverableNumber: Int
    val startPeriod: Int?
    val deactivated: Boolean?
    val description: String?
    val title: String?
}
