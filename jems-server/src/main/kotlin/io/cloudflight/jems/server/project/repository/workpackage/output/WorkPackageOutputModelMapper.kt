package io.cloudflight.jems.server.project.repository.workpackage

import io.cloudflight.jems.server.programme.entity.indicator.IndicatorOutput
import io.cloudflight.jems.server.project.entity.TranslationWorkPackageOutputId
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputEntity
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputId
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputTransl
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutputTranslatedValue
import kotlin.collections.HashSet

fun WorkPackageOutput.toEntity(
    workPackageId: Long,
    index: Int,
    resolveProgrammeIndicator: (Long?) -> IndicatorOutput?,
) : WorkPackageOutputEntity {
    val outputId = WorkPackageOutputId(workPackageId, index)
    return WorkPackageOutputEntity(
        outputId = outputId,
        translatedValues = translatedValues.toEntity(outputId),
        periodNumber = periodNumber,
        programmeOutputIndicator = resolveProgrammeIndicator.invoke(programmeOutputIndicatorId),
        targetValue = targetValue
    )
}

fun List<WorkPackageOutput>.toIndexedEntity(
    workPackageId: Long,
    resolveProgrammeIndicator: (Long?) -> IndicatorOutput?,
) = mapIndexed { index, output -> output.toEntity(workPackageId, index.plus(1), resolveProgrammeIndicator) }

fun WorkPackageOutputEntity.toModel() = WorkPackageOutput(
    outputNumber = outputId.outputNumber,
    translatedValues = translatedValues.toModel(),
    periodNumber = periodNumber,
    programmeOutputIndicatorId = programmeOutputIndicator?.id,
    targetValue = targetValue
)

fun Iterable<WorkPackageOutputEntity>.toModel() =
    this.map { it.toModel() }.sortedBy { it.outputNumber }.toList()

// region translations

fun Set<WorkPackageOutputTranslatedValue>.toEntity(outputId: WorkPackageOutputId) = mapTo(HashSet()) { it.toEntity(outputId) }

fun WorkPackageOutputTranslatedValue.toEntity(workPackageOutputId: WorkPackageOutputId) = WorkPackageOutputTransl(
    translationId = TranslationWorkPackageOutputId(workPackageOutputId = workPackageOutputId, language = language),
    title = title,
    description = description,
)

fun Set<WorkPackageOutputTransl>.toModel() = mapTo(HashSet()) {
    WorkPackageOutputTranslatedValue(
        language = it.translationId.language,
        description = it.description,
        title = it.title
    )
}

// endregion translations
