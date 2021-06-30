package io.cloudflight.jems.server.call.repository

import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy
import io.cloudflight.jems.api.programme.dto.strategy.ProgrammeStrategy
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.call.entity.ApplicationFormFieldConfigurationEntity
import io.cloudflight.jems.server.call.entity.ApplicationFormFieldConfigurationId
import io.cloudflight.jems.server.call.entity.CallEntity
import io.cloudflight.jems.server.call.entity.CallTranslEntity
import io.cloudflight.jems.server.call.entity.FlatRateSetupId
import io.cloudflight.jems.server.call.entity.ProjectCallFlatRateEntity
import io.cloudflight.jems.server.call.service.model.ApplicationFormFieldConfiguration
import io.cloudflight.jems.server.call.service.model.Call
import io.cloudflight.jems.server.call.service.model.CallDetail
import io.cloudflight.jems.server.call.service.model.CallSummary
import io.cloudflight.jems.server.call.service.model.IdNamePair
import io.cloudflight.jems.server.call.service.model.ProjectCallFlatRate
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.common.entity.extractField
import io.cloudflight.jems.server.programme.entity.ProgrammeSpecificObjectiveEntity
import io.cloudflight.jems.server.programme.entity.ProgrammeStrategyEntity
import io.cloudflight.jems.server.programme.entity.fund.ProgrammeFundEntity
import io.cloudflight.jems.server.programme.repository.costoption.toModel
import io.cloudflight.jems.server.programme.repository.fund.toModel
import io.cloudflight.jems.server.programme.repository.priority.toModel
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammePriority
import io.cloudflight.jems.server.project.repository.toModel
import io.cloudflight.jems.server.user.entity.UserEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.Page
import java.util.TreeSet
import kotlin.collections.HashSet

fun CallEntity.toModel() = CallSummary(
    id = id,
    name = name,
    status = status,
    startDate = startDate,
    endDate = endDate,
    endDateStep1 = endDateStep1
)

fun Page<CallEntity>.toModel() = map { it.toModel() }

fun CallEntity.toDetailModel(applicationFormFieldConfigurationEntities: MutableSet<ApplicationFormFieldConfigurationEntity>) = CallDetail(
    id = id,
    name = name,
    status = status,
    startDate = startDate,
    endDateStep1 = endDateStep1,
    endDate = endDate,
    isAdditionalFundAllowed = isAdditionalFundAllowed,
    lengthOfPeriod = lengthOfPeriod,
    description = translatedValues.extractField { it.description },
    objectives = prioritySpecificObjectives.groupSpecificObjectives(),
    strategies = strategies.mapTo(TreeSet()) { it.strategy },
    funds = funds.toModel(),
    flatRates = flatRates.toModel(),
    lumpSums = lumpSums.toModel(),
    unitCosts = unitCosts.toModel(),
    applicationFormFieldConfigurations = applicationFormFieldConfigurationEntities.toModel()
)

private fun Set<ProgrammeSpecificObjectiveEntity>.groupSpecificObjectives() =
    groupBy { it.programmePriority!!.id }.values.map {
        ProgrammePriority(
            id = it.first().programmePriority!!.id,
            code = it.first().programmePriority!!.code,
            title = it.first().programmePriority!!.translatedValues.mapTo(HashSet()) {
                InputTranslation(
                    it.translationId.language,
                    it.title
                )
            },
            objective = it.first().programmePriority!!.objective,
            specificObjectives = it.sortedBy { it.programmeObjectivePolicy }.map { it.toModel() }
        )
    }.sortedBy { it.id }

fun Call.toEntity(
    user: UserEntity,
    retrieveSpecificObjective: (ProgrammeObjectivePolicy) -> ProgrammeSpecificObjectiveEntity,
    retrieveStrategies: (Set<ProgrammeStrategy>) -> Set<ProgrammeStrategyEntity>,
    retrieveFunds: (Set<Long>) -> Set<ProgrammeFundEntity>,
    existingEntity: CallEntity? = null
) = CallEntity(
    id = id,
    creator = user,
    name = name,
    status = status!!,
    startDate = startDate,
    endDateStep1 = endDateStep1,
    endDate = endDate,
    lengthOfPeriod = lengthOfPeriod,
    isAdditionalFundAllowed = isAdditionalFundAllowed,
    translatedValues = mutableSetOf(),
    prioritySpecificObjectives = priorityPolicies.mapTo(HashSet()) { retrieveSpecificObjective.invoke(it) },
    strategies = retrieveStrategies.invoke(strategies).toMutableSet(),
    funds = retrieveFunds.invoke(fundIds).toMutableSet(),
    flatRates = existingEntity?.flatRates ?: mutableSetOf(),
    lumpSums = existingEntity?.lumpSums ?: mutableSetOf(),
    unitCosts = existingEntity?.unitCosts ?: mutableSetOf()
).apply {
    translatedValues.addAll(description.combineDescriptionsToTranslations(this))
}

fun Set<InputTranslation>.combineDescriptionsToTranslations(call: CallEntity): Set<CallTranslEntity> =
    mapTo(HashSet()) {
        CallTranslEntity(
            translationId = TranslationId(sourceEntity = call, language = it.language),
            description = it.translation
        )
    }

fun Set<ProjectCallFlatRateEntity>.toModel() = mapTo(TreeSet()) {
    ProjectCallFlatRate(
        type = it.setupId.type,
        rate = it.rate,
        isAdjustable = it.isAdjustable
    )
}

fun Set<ProjectCallFlatRate>.toEntity(call: CallEntity) = mapTo(HashSet()) {
    ProjectCallFlatRateEntity(
        setupId = FlatRateSetupId(call = call, type = it.type),
        rate = it.rate,
        isAdjustable = it.isAdjustable
    )
}


fun MutableSet<ApplicationFormFieldConfigurationEntity>.toModel() =
    callEntityMapper.map(this)

fun MutableSet<ApplicationFormFieldConfiguration>.toEntities(call: CallEntity) =
    map { callEntityMapper.map(call, it) }.toMutableSet()

fun List<CallEntity>.toIdNamePair() =
    callEntityMapper.map(this)


private val callEntityMapper = Mappers.getMapper(CallEntityMapper::class.java)

@Mapper
abstract class CallEntityMapper {

    abstract fun map(calls: List<CallEntity>): List<IdNamePair>

    @Mapping(source = "id.id", target = "id")
    abstract fun map(applicationFormFieldConfigurationEntity: ApplicationFormFieldConfigurationEntity): ApplicationFormFieldConfiguration

    abstract fun map(applicationFormFieldConfigurationEntities: MutableSet<ApplicationFormFieldConfigurationEntity>): MutableSet<ApplicationFormFieldConfiguration>


    fun map(
        call: CallEntity,
        fieldConfiguration: ApplicationFormFieldConfiguration
    ): ApplicationFormFieldConfigurationEntity =
        ApplicationFormFieldConfigurationEntity(
            ApplicationFormFieldConfigurationId(fieldConfiguration.id, call),
            fieldConfiguration.visibilityStatus
        )

}
