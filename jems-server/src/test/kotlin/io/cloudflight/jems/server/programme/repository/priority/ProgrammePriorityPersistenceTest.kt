package io.cloudflight.jems.server.programme.repository.priority

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjective.PO2
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjective.PO3
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy.CrossBorderMobility
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy.GreenInfrastructure
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy.InterModalTenT
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy.RenewableEnergy
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy.WaterManagement
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.call.repository.CallRepository
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.ProgrammePriorityEntity
import io.cloudflight.jems.server.programme.entity.ProgrammeSpecificObjectiveEntity
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammeObjectiveDimension
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammePriority
import io.cloudflight.jems.server.programme.service.priority.model.ProgrammeSpecificObjective
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class ProgrammePriorityPersistenceTest : UnitTest() {

    companion object {
        private const val ID = 1L
        private val _priority = ProgrammePriorityEntity(
            id = ID,
            code = "Code 10",
            translatedValues = combineTranslatedValues(
                programmePriorityId = ID,
                title = setOf(InputTranslation(SystemLanguage.EN, "10 Title"))
            ),
            objective = PO2,
        )

        private val priority = _priority.copy(
            specificObjectives = setOf(
                ProgrammeSpecificObjectiveEntity(programmeObjectivePolicy = GreenInfrastructure, code = "GU", programmePriority = _priority),
                ProgrammeSpecificObjectiveEntity(programmeObjectivePolicy = WaterManagement, code = "WM", programmePriority = _priority),
            )
        )

        private val priorityModel = ProgrammePriority(
            id = ID,
            code = "Code 10",
            title = setOf(InputTranslation(SystemLanguage.EN, "10 Title")),
            objective = PO2,
            specificObjectives = listOf(
                ProgrammeSpecificObjective(programmeObjectivePolicy = WaterManagement, code = "WM"),
                ProgrammeSpecificObjective(programmeObjectivePolicy = GreenInfrastructure, code = "GU"),
            )
        )

    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(specificObjectiveRepository)
    }

    @MockK
    lateinit var priorityRepository: ProgrammePriorityRepository

    @RelaxedMockK
    lateinit var specificObjectiveRepository: ProgrammeSpecificObjectiveRepository

    @MockK
    lateinit var callRepository: CallRepository

    @InjectMockKs
    private lateinit var persistence: ProgrammePriorityPersistenceProvider

    @Test
    fun getPriorityById() {
        every { priorityRepository.findById(ID) } returns Optional.of(priority)
        assertThat(persistence.getPriorityById(ID)).isEqualTo(priorityModel)
    }

    @Test
    fun `getPriorityById - not existing`() {
        every { priorityRepository.findById(-1) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { persistence.getPriorityById(-1) }
    }

    @Test
    fun getAllPriorities() {
        every { priorityRepository.findTop56ByOrderByCodeAsc() } returns listOf(priority)
        assertThat(persistence.getAllMax56Priorities()).containsExactly(priorityModel)
    }

    @Test
    fun create() {
        val entitySentToSave = slot<ProgrammePriorityEntity>()
        every { priorityRepository.save(capture(entitySentToSave)) } returnsArgument 0
        every { specificObjectiveRepository.save(any())} returnsArgument 0

        val toSave = priorityModel.copy(id = null)
        assertThat(persistence.create(toSave)).isEqualTo(priorityModel.copy(id = 0))
        assertThat(entitySentToSave.captured).isEqualTo(priority.copy(
            id = 0,
            translatedValues = combineTranslatedValues(
                programmePriorityId = 0,
                title = setOf(InputTranslation(SystemLanguage.EN, "10 Title"))
            )
        ))

        verify(exactly = 2) { specificObjectiveRepository.save(any()) }
    }

    @Test
    fun update() {
        val entitySentToSave = slot<ProgrammePriorityEntity>()
        val ID_UPDATED = 15L
        every { priorityRepository.existsById(ID_UPDATED) } returns true
        every { priorityRepository.save(capture(entitySentToSave)) } returnsArgument 0
        every { specificObjectiveRepository.save(any())} returnsArgument 0

        val toSave = ProgrammePriority(
            id = ID_UPDATED,
            code = "updated code",
            title = setOf(InputTranslation(SystemLanguage.EN, "updated title")),
            objective = PO3,
            specificObjectives = listOf(
                ProgrammeSpecificObjective(programmeObjectivePolicy = InterModalTenT, code = "IM10T"),
                ProgrammeSpecificObjective(programmeObjectivePolicy = CrossBorderMobility, code = "CBM"),
            )
        )

        assertThat(persistence.update(toSave)).isEqualTo(toSave)
        assertThat(entitySentToSave.captured).isEqualTo(ProgrammePriorityEntity(
            id = ID_UPDATED,
            code = "updated code",
            translatedValues = combineTranslatedValues(
                programmePriorityId = ID_UPDATED,
                title = setOf(InputTranslation(SystemLanguage.EN, "updated title"))
            ),
            objective = PO3,
            specificObjectives = setOf(
                ProgrammeSpecificObjectiveEntity(programmeObjectivePolicy = InterModalTenT, code = "IM10T"),
                ProgrammeSpecificObjectiveEntity(programmeObjectivePolicy = CrossBorderMobility, code = "CBM"),
            )
        ))

        verify(exactly = 2) { specificObjectiveRepository.save(any()) }
    }

    @Test
    fun `update - not existing`() {
        every { priorityRepository.existsById(-1) } returns false
        assertThrows<ResourceNotFoundException> { persistence.update(priorityModel.copy(id = -1)) }
    }

    @Test
    fun delete() {
        every { priorityRepository.findById(ID) } returns Optional.of(priority)
        val entitySentToDelete = slot<ProgrammePriorityEntity>()
        every { priorityRepository.delete(capture(entitySentToDelete)) } answers { }
        persistence.delete(ID)
        assertThat(entitySentToDelete.captured.id).isEqualTo(ID)
    }

    @Test
    fun `getPriorityIdByCode - existing`() {
        every { priorityRepository.findFirstByCode("existing code") } returns priority.copy(code = "existing code")
        assertThat(persistence.getPriorityIdByCode("existing code")).isEqualTo(ID)
    }

    @Test
    fun `getPriorityIdByCode - not existing`() {
        every { priorityRepository.findFirstByCode("not existing code") } returns null
        assertThat(persistence.getPriorityIdByCode("not existing code")).isNull()
    }

    @Test
    fun getPriorityIdForPolicyIfExists() {
        every { specificObjectiveRepository.getPriorityIdForPolicyIfExists(RenewableEnergy) } returns ID
        every { specificObjectiveRepository.getPriorityIdForPolicyIfExists(GreenInfrastructure) } returns null
        assertThat(persistence.getPriorityIdForPolicyIfExists(RenewableEnergy)).isEqualTo(ID)
        assertThat(persistence.getPriorityIdForPolicyIfExists(GreenInfrastructure)).isNull()
    }

    @Test
    fun getSpecificObjectivesByCodes() {
        every { specificObjectiveRepository.findAllByCodeIn(emptySet()) } returns emptySet()
        every { specificObjectiveRepository.findAllByCodeIn(setOf("GU")) } returns setOf(
            ProgrammeSpecificObjectiveEntity(
                programmeObjectivePolicy = GreenInfrastructure,
                code = "GU",
                programmePriority = priority
            )
        )

        assertThat(persistence.getSpecificObjectivesByCodes(emptySet())).isEmpty()
        assertThat(persistence.getSpecificObjectivesByCodes(setOf("GU"))).containsExactly(
            ProgrammeSpecificObjective(
                programmeObjectivePolicy = GreenInfrastructure,
                code = "GU"
            )
        )
    }

    @Test
    fun getPrioritiesBySpecificObjectiveCodes() {
        every { specificObjectiveRepository.findAllByCodeIn(setOf("RE", "WM")) } returns setOf(
            ProgrammeSpecificObjectiveEntity(
                programmeObjectivePolicy = GreenInfrastructure,
                code = "GU",
                programmePriority = priority.copy()
            ),
            ProgrammeSpecificObjectiveEntity(
                programmeObjectivePolicy = WaterManagement,
                code = "WM",
                programmePriority = priority.copy()
            ),
        )

        assertThat(persistence.getPrioritiesBySpecificObjectiveCodes(setOf("RE", "WM"))).containsExactly(
            priorityModel
        )
    }

    @Test
    fun getObjectivePoliciesAlreadySetUp() {
        every { specificObjectiveRepository.findTop56ByOrderByProgrammeObjectivePolicy() } returns listOf(
            ProgrammeSpecificObjectiveEntity(
                programmeObjectivePolicy = GreenInfrastructure,
                code = "GU",
                programmePriority = priority
            ),
            ProgrammeSpecificObjectiveEntity(
                programmeObjectivePolicy = WaterManagement,
                code = "WM",
                programmePriority = priority
            ),
        )

        assertThat(persistence.getObjectivePoliciesAlreadySetUp()).containsExactlyInAnyOrder(GreenInfrastructure, WaterManagement)
    }

    @Test
    fun getObjectivePoliciesAlreadyInUse() {
        every { specificObjectiveRepository.getObjectivePoliciesAlreadyInUse() } returns listOf(
            GreenInfrastructure.name,
            WaterManagement.name,
        )
        assertThat(persistence.getObjectivePoliciesAlreadyInUse()).containsExactly(GreenInfrastructure, WaterManagement)
    }

}
