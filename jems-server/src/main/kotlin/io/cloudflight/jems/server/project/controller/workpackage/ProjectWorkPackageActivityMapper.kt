package io.cloudflight.jems.server.project.controller.workpackage

import io.cloudflight.jems.api.project.dto.workpackage.activity.WorkPackageActivityDTO
import io.cloudflight.jems.api.project.dto.workpackage.activity.WorkPackageActivityDeliverableDTO
import io.cloudflight.jems.api.project.dto.workpackage.activity.WorkPackageActivitySummaryDTO
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivitySummary
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityDeliverable

fun WorkPackageActivityDTO.toModel(workPackageId: Long) = WorkPackageActivity(
    id = id,
    workPackageId = workPackageId,
    title = title,
    description = description,
    startPeriod = startPeriod,
    endPeriod = endPeriod,
    deliverables = deliverables.toDeliverableModel(),
    partnerIds = partnerIds,
    deactivated = deactivated
)

fun List<WorkPackageActivityDTO>.toModel(workPackageId: Long) = map { it.toModel(workPackageId) }

fun WorkPackageActivityDeliverableDTO.toDeliverableModel(number: Int) = WorkPackageActivityDeliverable(
    id = deliverableId,
    deliverableNumber = number,
    description = description,
    title = title,
    period = period,
    deactivated = deactivated
)

fun List<WorkPackageActivityDeliverableDTO>.toDeliverableModel() = mapIndexed { index, it -> it.toDeliverableModel(index.plus(1)) }


fun WorkPackageActivity.toDto() = WorkPackageActivityDTO(
    id = id,
    workPackageId = workPackageId,
    activityNumber = activityNumber,
    title = title,
    startPeriod = startPeriod,
    endPeriod = endPeriod,
    description = description,
    deliverables = deliverables.toDeliverableDto(id),
    partnerIds = partnerIds,
    deactivated = deactivated
)

fun List<WorkPackageActivity>.toDto() = map { it.toDto() }

fun WorkPackageActivityDeliverable.toDeliverableDto(activityId: Long) = WorkPackageActivityDeliverableDTO(
    activityId = activityId,
    deliverableId = id,
    deliverableNumber = deliverableNumber,
    description = description,
    title = title,
    period = period,
    deactivated = deactivated
)

fun List<WorkPackageActivityDeliverable>.toDeliverableDto(activityId: Long) = map { it.toDeliverableDto(activityId) }

fun WorkPackageActivitySummary.toDto() = WorkPackageActivitySummaryDTO(
    activityId = activityId,
    workPackageNumber = workPackageNumber,
    activityNumber = activityNumber
)

fun List<WorkPackageActivitySummary>.toSummariesDto() = this.map { it.toDto() }
