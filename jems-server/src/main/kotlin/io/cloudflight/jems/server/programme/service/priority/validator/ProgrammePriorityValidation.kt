package io.cloudflight.jems.server.programme.service.priority.validator

import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjective
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy
import io.cloudflight.jems.server.common.exception.I18nFieldError
import io.cloudflight.jems.server.common.exception.I18nValidationException
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammePriority
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammeSpecificObjective
import org.springframework.http.HttpStatus

fun validateCreateProgrammePriority(
    programmePriority: ProgrammePriority,
    getPriorityIdByCode: (String) -> Long?,
    getPriorityIdByTitle: (String) -> Long?,
    getPriorityIdForPolicyIfExists: (ProgrammeObjectivePolicy) -> Long?,
    getSpecificObjectivesByCodes: (Collection<String>) -> List<ProgrammeSpecificObjective>,
) {
    validateCommonRestrictions(programmePriority)

    validateCreateHasUniqueCodeAndTitle(
        programmePriority = programmePriority,
        getPriorityIdByCode = getPriorityIdByCode,
        getPriorityIdByTitle = getPriorityIdByTitle,
    )
    validateEveryPolicyIsFree(
        policies = programmePriority.specificObjectives.mapTo(HashSet()) { it.programmeObjectivePolicy },
        getPriorityIdForPolicyIfExists = getPriorityIdForPolicyIfExists,
    )
    validateEveryPolicyCodeIsFree(
        policyCodes = programmePriority.specificObjectives.mapTo(HashSet()) { it.code },
        getSpecificObjectivesByCodes = getSpecificObjectivesByCodes,
    )
}

fun validateUpdateProgrammePriority(
    programmePriorityId: Long,
    programmePriority: ProgrammePriority,
    getPriorityIdByCode: (String) -> Long?,
    getPriorityIdByTitle: (String) -> Long?,
    getPriorityIdForPolicyIfExists: (ProgrammeObjectivePolicy) -> Long?,
    getPrioritiesBySpecificObjectiveCodes: (Collection<String>) -> List<ProgrammePriority>,
) {
    validateCommonRestrictions(programmePriority)

    validateUpdateHasUniqueCodeAndTitle(
        priorityId = programmePriorityId,
        programmePriority = programmePriority,
        getPriorityIdByCode = getPriorityIdByCode,
        getPriorityIdByTitle = getPriorityIdByTitle,
    )
    validateEveryPolicyIsFreeOrLinkedToThisPriority(
        priorityId = programmePriorityId,
        policies = programmePriority.specificObjectives.mapTo(HashSet()) { it.programmeObjectivePolicy },
        getPriorityIdForPolicyIfExists = getPriorityIdForPolicyIfExists,
    )
    validateEveryPolicyCodeIsFreeOrLinkedToThisPriority(
        priorityId = programmePriorityId,
        policyCodes = programmePriority.specificObjectives.mapTo(HashSet()) { it.code },
        getPrioritiesBySpecificObjectiveCodes = getPrioritiesBySpecificObjectiveCodes,
    )
}

private fun validateCommonRestrictions(programmePriority: ProgrammePriority) {
    val code = programmePriority.code
    if (code.isBlank() || code.length > 50)
        invalid("programme.priority.code.size.too.long")

    val title = programmePriority.title
    if (title.isBlank() || title.length > 300)
        invalid("programme.priority.title.size.too.long")

    if (programmePriority.specificObjectives.isEmpty())
        invalid("programme.priority.specificObjectives.empty")

    val wrongSpecificObjectives = programmePriority.specificObjectives.filter {
        it.code.isBlank() || it.code.length > 50
    }.map { it.programmeObjectivePolicy.name }

    if (wrongSpecificObjectives.isNotEmpty())
        invalid(
            fieldErrors = mapOf(
                "specificObjectives" to I18nFieldError(
                    i18nKey = "programme.priority.specificObjective.code.size.too.long.or.empty",
                    i18nArguments = wrongSpecificObjectives
                )
            )
        )

    validateSpecificObjectivePoliciesAreFromCorrectProgrammeObjective(
        specificObjectives = programmePriority.specificObjectives,
        programmeObjective = programmePriority.objective,
    )
    validateSpecificObjectivesAreUnique(
        specificObjectives = programmePriority.specificObjectives
    )
}

private fun validateCreateHasUniqueCodeAndTitle(
    programmePriority: ProgrammePriority,
    getPriorityIdByCode: (String) -> Long?,
    getPriorityIdByTitle: (String) -> Long?,
) {
    val priorityIdWithSameCode = getPriorityIdByCode.invoke(programmePriority.code)
    if (priorityIdWithSameCode != null)
        invalid("programme.priority.code.already.in.use")

    val priorityIdWithSameTitle = getPriorityIdByTitle.invoke(programmePriority.title)
    if (priorityIdWithSameTitle != null)
        invalid("programme.priority.title.already.in.use")
}

private fun validateUpdateHasUniqueCodeAndTitle(
    priorityId: Long,
    programmePriority: ProgrammePriority,
    getPriorityIdByCode: (String) -> Long?,
    getPriorityIdByTitle: (String) -> Long?,
) {
    val priorityIdWithSameCode = getPriorityIdByCode.invoke(programmePriority.code)
    if (priorityIdWithSameCode != null && priorityIdWithSameCode != priorityId)
        invalid("programme.priority.code.already.in.use")

    val priorityIdWithSameTitle = getPriorityIdByTitle.invoke(programmePriority.title)
    if (priorityIdWithSameTitle != null && priorityIdWithSameTitle != priorityId)
        invalid("programme.priority.title.already.in.use")
}

private fun validateSpecificObjectivePoliciesAreFromCorrectProgrammeObjective(
    specificObjectives: Collection<ProgrammeSpecificObjective>,
    programmeObjective: ProgrammeObjective,
) {
    if (specificObjectives.any { it.programmeObjectivePolicy.objective != programmeObjective })
        invalid("programme.priority.specificObjectives.should.not.be.of.different.objectives")
}

private fun validateSpecificObjectivesAreUnique(specificObjectives: Collection<ProgrammeSpecificObjective>) {
    val codes = specificObjectives.mapTo(HashSet()) { it.code }
    val policies = specificObjectives.mapTo(HashSet()) { it.programmeObjectivePolicy }

    if (codes.size != specificObjectives.size)
        invalid("programme.priority.specificObjective.code.should.be.unique")
    if (policies.size != specificObjectives.size)
        invalid("programme.priority.specificObjective.objectivePolicy.should.be.unique")
}

private fun validateEveryPolicyIsFree(
    policies: Set<ProgrammeObjectivePolicy>,
    getPriorityIdForPolicyIfExists: (ProgrammeObjectivePolicy) -> Long?
) {
    val policiesInUse = policies.filter { getPriorityIdForPolicyIfExists.invoke(it) != null }.map { it.name }
    if (policiesInUse.isNotEmpty())
        invalid(
            fieldErrors = mapOf(
                "specificObjectives" to I18nFieldError(
                    i18nKey = "programme.priority.specificObjective.objectivePolicy.already.in.use",
                    i18nArguments = policiesInUse
                )
            )
        )
}

private fun validateEveryPolicyCodeIsFree(
    policyCodes: Set<String>,
    getSpecificObjectivesByCodes: (Collection<String>) -> List<ProgrammeSpecificObjective>
) {
    val existingPolicyCodes = getSpecificObjectivesByCodes.invoke(policyCodes).map { it.code }

    if (existingPolicyCodes.isNotEmpty())
        invalid(
            fieldErrors = mapOf(
                "specificObjectives" to I18nFieldError(
                    i18nKey = "programme.priority.specificObjective.code.already.in.use",
                    i18nArguments = existingPolicyCodes
                )
            )
        )
}

private fun validateEveryPolicyIsFreeOrLinkedToThisPriority(
    priorityId: Long,
    policies: Set<ProgrammeObjectivePolicy>,
    getPriorityIdForPolicyIfExists: (ProgrammeObjectivePolicy) -> Long?
) {
    val policiesInUse = mutableListOf<String>()

    policies.forEach {
        val priorityIdOfThisPolicy = getPriorityIdForPolicyIfExists.invoke(it)
        if (priorityIdOfThisPolicy != null && priorityIdOfThisPolicy != priorityId) {
            policiesInUse.add(it.name)
        }
    }

    if (policiesInUse.isNotEmpty())
        invalid(
            fieldErrors = mapOf(
                "specificObjectives" to I18nFieldError(
                    i18nKey = "programme.priority.specificObjective.objectivePolicy.already.in.use",
                    i18nArguments = policiesInUse
                )
            )
        )
}

private fun validateEveryPolicyCodeIsFreeOrLinkedToThisPriority(
    priorityId: Long,
    policyCodes: Set<String>,
    getPrioritiesBySpecificObjectiveCodes: (Collection<String>) -> List<ProgrammePriority>
) {
    val priorities = getPrioritiesBySpecificObjectiveCodes(policyCodes).filter {
        it.id!! != priorityId
    }.map { it.code }

    if (priorities.isNotEmpty())
        invalid(
            fieldErrors = mapOf(
                "specificObjectives" to I18nFieldError(
                    i18nKey = "programme.priority.specificObjective.code.already.in.use.by.other.priority",
                    i18nArguments = priorities
                )
            )
        )
}

fun checkNoCallExistsForRemovedSpecificObjectives(
    newObjectivePolicies: Set<ProgrammeObjectivePolicy>,
    existingPriority: ProgrammePriority,
    alreadyUsedObjectivePolicies: Iterable<ProgrammeObjectivePolicy>
) {
    val objectivePoliciesToBeRemoved = existingPriority.specificObjectives.mapTo(HashSet()) { it.programmeObjectivePolicy }
    objectivePoliciesToBeRemoved.removeAll(newObjectivePolicies)

    val policiesThatCannotBeRemoved = alreadyUsedObjectivePolicies intersect objectivePoliciesToBeRemoved
    if (policiesThatCannotBeRemoved.isNotEmpty())
        invalid(fieldErrors = mapOf(
            "specificObjectives" to I18nFieldError(
                i18nKey = "programme.priority.specificObjective.already.used.in.call",
                i18nArguments = policiesThatCannotBeRemoved.map { it.name }
            )
        ))
}

private fun invalid(message: String? = null, fieldErrors: Map<String, I18nFieldError> = emptyMap()) {
    throw I18nValidationException(
        httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
        i18nKey = message,
        i18nFieldErrors = fieldErrors
    )
}
