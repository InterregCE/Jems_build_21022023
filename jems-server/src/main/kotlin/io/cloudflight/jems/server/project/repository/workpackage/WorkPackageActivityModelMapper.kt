package io.cloudflight.jems.server.project.repository.workpackage

import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableId
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityId
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityTranslationEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityTranslationId
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableTranslationEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableTranslationId
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityDeliverable
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityDeliverableTranslatedValue
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityTranslatedValue

fun WorkPackageActivity.toEntity(workPackageId: Long, index: Int): WorkPackageActivityEntity {
    val activityId = WorkPackageActivityId(workPackageId, index)
    return WorkPackageActivityEntity(
        activityId = activityId,
        translatedValues = translatedValues.toEntity(activityId),
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        deliverables = deliverables.toIndexedEntity(activityId),
    )
}

fun List<WorkPackageActivity>.toIndexedEntity(workPackageId: Long) =
    mapIndexed { index, activity -> activity.toEntity(workPackageId, index.plus(1)) }

fun WorkPackageActivityTranslatedValue.toEntity(activityId: WorkPackageActivityId) = WorkPackageActivityTranslationEntity(
    translationId = WorkPackageActivityTranslationId(activityId = activityId, language = language),
    title = title,
    description = description,
)
fun Set<WorkPackageActivityTranslatedValue>.toEntity(activityId: WorkPackageActivityId) = mapTo(HashSet()) { it.toEntity(activityId) }

fun WorkPackageActivityDeliverable.toEntity(activityId: WorkPackageActivityId, index: Int): WorkPackageActivityDeliverableEntity {
    val deliverableId = WorkPackageActivityDeliverableId(activityId = activityId, deliverableNumber = index)
    return WorkPackageActivityDeliverableEntity(
        deliverableId = deliverableId,
        translatedValues = translatedValues.toEntity(deliverableId),
        startPeriod = period,
    )
}

fun List<WorkPackageActivityDeliverable>.toIndexedEntity(activityId: WorkPackageActivityId) =
    mapIndexed { index, deliverable -> deliverable.toEntity(activityId, index.plus(1)) }

fun WorkPackageActivityDeliverableTranslatedValue.toEntity(deliverableId: WorkPackageActivityDeliverableId) = WorkPackageActivityDeliverableTranslationEntity(
    translationId = WorkPackageActivityDeliverableTranslationId(deliverableId = deliverableId, language = language),
    description = description,
)
fun Set<WorkPackageActivityDeliverableTranslatedValue>.toEntity(deliverableId: WorkPackageActivityDeliverableId) = mapTo(HashSet()) { it.toEntity(deliverableId) }

fun WorkPackageActivityEntity.toModel() = WorkPackageActivity(
    translatedValues = translatedValues.toModel(),
    startPeriod = startPeriod,
    endPeriod = endPeriod,
    deliverables = deliverables.sortedBy { it.deliverableId.deliverableNumber }.map { it.toModel() },
)

fun List<WorkPackageActivityEntity>.toModel() = sortedBy { it.activityId.activityNumber }.map { it.toModel() }

fun WorkPackageActivityDeliverableEntity.toModel() = WorkPackageActivityDeliverable(
    translatedValues = translatedValues.toDeliverableModel(),
    period = startPeriod,
)

fun Set<WorkPackageActivityTranslationEntity>.toModel() = mapTo(HashSet()) {
    WorkPackageActivityTranslatedValue(
        language = it.translationId.language,
        title = it.title,
        description = it.description,
    )
}
fun Set<WorkPackageActivityDeliverableTranslationEntity>.toDeliverableModel() = mapTo(HashSet()) {
    WorkPackageActivityDeliverableTranslatedValue(
        language = it.translationId.language,
        description = it.description,
    )
}