package io.cloudflight.jems.server.project.service.workpackage.model

import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput

data class ProjectWorkPackage(
    val id: Long,
    val workPackageNumber: Int,
    val translatedValues: Set<ProjectWorkPackageTranslatedValue> = emptySet(),
    var activities: List<WorkPackageActivity> = emptyList(),
    var outputs: List<WorkPackageOutput> = emptyList()
)
