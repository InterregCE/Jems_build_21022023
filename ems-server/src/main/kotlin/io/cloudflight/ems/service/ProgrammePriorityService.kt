package io.cloudflight.ems.service

import io.cloudflight.ems.api.programme.dto.InputProgrammePriorityCreate
import io.cloudflight.ems.api.programme.dto.InputProgrammePriorityUpdate
import io.cloudflight.ems.api.programme.dto.OutputProgrammePriority
import io.cloudflight.ems.api.programme.dto.OutputProgrammePriorityPolicy
import io.cloudflight.ems.api.programme.dto.ProgrammeObjective
import io.cloudflight.ems.api.programme.dto.ProgrammeObjectivePolicy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProgrammePriorityService {

    fun getAll(page: Pageable): Page<OutputProgrammePriority>

    fun create(priority: InputProgrammePriorityCreate): OutputProgrammePriority

    fun update(priority: InputProgrammePriorityUpdate): OutputProgrammePriority

    fun delete(programmePriorityId: Long)

    /**
     * Will give you all objective policies, that are not yet taken by any priority ( = are now free to take)
     */
    fun getFreePrioritiesWithPolicies(): Map<ProgrammeObjective, List<ProgrammeObjectivePolicy>>

    fun getByCode(priorityCode: String): OutputProgrammePriority?

    fun getByTitle(title: String): OutputProgrammePriority?

    /**
     * Will give you
     *   - only a ProgrammePriorityPolicy for given code without ProgrammePriority assigned to it
     *   - or null if there is not yet a ProgrammePriorityPolicy with such code
     */
    fun getPriorityPolicyByCode(code: String): OutputProgrammePriorityPolicy?

    /**
     * Will extract ProgrammePriority ID, if given ProgrammePriorityPolicy is already defined in programme setup.
     */
    fun getPriorityIdForPolicyIfExists(policy: ProgrammeObjectivePolicy): Long?

}
