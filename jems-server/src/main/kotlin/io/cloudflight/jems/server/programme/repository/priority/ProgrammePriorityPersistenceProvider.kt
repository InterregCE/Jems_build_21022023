package io.cloudflight.jems.server.programme.repository.priority

import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.ProgrammeObjectiveDimensionCodeEntity
import io.cloudflight.jems.server.programme.entity.ProgrammePriorityObjectiveDimensionCodeId
import io.cloudflight.jems.server.programme.entity.ProgrammeSpecificObjectiveEntity
import io.cloudflight.jems.server.programme.service.priority.ProgrammePriorityPersistence
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammeObjectiveDimension
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammePriority
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammeSpecificObjective
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProgrammePriorityPersistenceProvider(
    private val priorityRepo: ProgrammePriorityRepository,
    private val specificObjectiveRepo: ProgrammeSpecificObjectiveRepository
) : ProgrammePriorityPersistence {

    @Transactional(readOnly = true)
    override fun getPriorityById(priorityId: Long): ProgrammePriority =
        getPriorityOrThrow(priorityId).toModel()

    @Transactional(readOnly = true)
    override fun getAllMax56Priorities(): List<ProgrammePriority> =
        priorityRepo.findTop56ByOrderByCodeAsc().map { it.toModel() }

    @Transactional
    override fun create(priority: ProgrammePriority): ProgrammePriority {
        val priorityCreated = priorityRepo.save(priority.toEntity())

        priorityCreated.specificObjectives.onEach {
            specificObjectiveRepo.save(
                it.copy(
                    dimensionCodes = priority.specificObjectives
                        .find { obj -> it.code == obj.code }!!.dimensionCodes.toEntity(it)
                )
            )
        }

        return priorityRepo.save(
            priorityCreated.copy(translatedValues = combineTranslatedValues(priorityCreated.id, priority.title))
        ).toModel()
    }

    @Transactional
    override fun update(priority: ProgrammePriority): ProgrammePriority {
        if (priorityRepo.existsById(priority.id!!)) {
            val updated = priorityRepo.save(
                priority.toEntity().copy(
                    translatedValues = combineTranslatedValues(priority.id, priority.title)
                ))

            updated.specificObjectives.onEach {
                specificObjectiveRepo.save(
                    it.copy(
                        dimensionCodes = priority.specificObjectives
                            .find { obj -> it.code == obj.code }!!.dimensionCodes.toEntity(it)
                    )
                )
            }

            return updated.toModel()
        }
        else throw ResourceNotFoundException("programmePriority")
    }

    @Transactional
    override fun delete(priorityId: Long) =
        priorityRepo.delete(getPriorityOrThrow(priorityId))

    @Transactional(readOnly = true)
    override fun getPriorityIdByCode(code: String): Long? =
        priorityRepo.findFirstByCode(code)?.id

    @Transactional(readOnly = true)
    override fun getPriorityIdForPolicyIfExists(policy: ProgrammeObjectivePolicy): Long? =
        specificObjectiveRepo.getPriorityIdForPolicyIfExists(policy)

    @Transactional(readOnly = true)
    override fun getSpecificObjectivesByCodes(specificObjectiveCodes: Collection<String>): List<ProgrammeSpecificObjective> =
        specificObjectiveRepo.findAllByCodeIn(specificObjectiveCodes).map { it.toModel() }

    @Transactional(readOnly = true)
    override fun getPrioritiesBySpecificObjectiveCodes(specificObjectiveCodes: Collection<String>): List<ProgrammePriority> =
        specificObjectiveRepo.findAllByCodeIn(specificObjectiveCodes)
            .mapNotNullTo(HashSet()) { it.programmePriority }
            .map { it.toModel() }

    @Transactional(readOnly = true)
    override fun getObjectivePoliciesAlreadySetUp(): Iterable<ProgrammeObjectivePolicy> =
        specificObjectiveRepo.findTop56ByOrderByProgrammeObjectivePolicy().map { it.programmeObjectivePolicy }

    @Transactional(readOnly = true)
    override fun getObjectivePoliciesAlreadyInUse(): Iterable<ProgrammeObjectivePolicy> =
        specificObjectiveRepo.getObjectivePoliciesAlreadyInUse().map { ProgrammeObjectivePolicy.valueOf(it) }

    @Transactional(readOnly = true)
    override fun getObjectivePoliciesAlreadyUsedByResultIndicator(): Iterable<ProgrammeObjectivePolicy> =
        specificObjectiveRepo.getObjectivePoliciesAlreadyUsedByResultIndicator().map { ProgrammeObjectivePolicy.valueOf(it) }

    @Transactional(readOnly = true)
    override fun getObjectivePoliciesAlreadyUsedByOutputIndicator(): Iterable<ProgrammeObjectivePolicy> =
        specificObjectiveRepo.getObjectivePoliciesAlreadyUsedByOutputIndicator().map { ProgrammeObjectivePolicy.valueOf(it) }

    private fun getPriorityOrThrow(priorityId: Long) =
        priorityRepo.findById(priorityId).orElseThrow { ResourceNotFoundException("programmePriority") }

}
