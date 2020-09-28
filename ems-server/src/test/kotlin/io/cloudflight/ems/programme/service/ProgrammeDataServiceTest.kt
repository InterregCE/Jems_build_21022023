package io.cloudflight.ems.programme.service

import io.cloudflight.ems.api.programme.SystemLanguage
import io.cloudflight.ems.api.user.dto.OutputUserRole
import io.cloudflight.ems.api.user.dto.OutputUserWithRole
import io.cloudflight.ems.api.programme.dto.InputProgrammeData
import io.cloudflight.ems.api.programme.dto.OutputProgrammeData
import io.cloudflight.ems.api.programme.dto.SystemLanguageSelection
import io.cloudflight.ems.audit.entity.AuditAction
import io.cloudflight.ems.audit.service.AuditCandidate
import io.cloudflight.ems.entity.ProgrammeData
import io.cloudflight.ems.exception.ResourceNotFoundException
import io.cloudflight.ems.nuts.entity.NutsCountry
import io.cloudflight.ems.nuts.entity.NutsRegion1
import io.cloudflight.ems.nuts.entity.NutsRegion2
import io.cloudflight.ems.nuts.entity.NutsRegion3
import io.cloudflight.ems.nuts.repository.NutsRegion3Repository
import io.cloudflight.ems.nuts.service.NutsIdentifier
import io.cloudflight.ems.repository.ProgrammeDataRepository
import io.cloudflight.ems.audit.service.AuditService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.Optional

/**
 * tests ProgrammeDataService methods including ProgrammeDataMapper.
 */
internal class ProgrammeDataServiceTest {

    private val user = OutputUserWithRole(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        userRole = OutputUserRole(id = 1, name = "ADMIN")
    )

    private val existingProgrammeData =
        ProgrammeData(
            1,
            "cci",
            "title",
            "version",
            2020,
            2024,
            null, null,
            null,
            null,
            null,
            null,
            null
        )
    private val systemLanguages =
        existingProgrammeData.getSystemLanguageSelectionList()

    @MockK
    lateinit var programmeDataRepository: ProgrammeDataRepository
    @MockK
    lateinit var nutsRegion3Repository: NutsRegion3Repository

    @RelaxedMockK
    lateinit var auditService: AuditService
    lateinit var programmeDataService: ProgrammeDataService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        programmeDataService = ProgrammeDataServiceImpl(
            programmeDataRepository,
            nutsRegion3Repository,
            auditService
        )
    }

    @Test
    fun get() {
        val programmeDataInput =
            OutputProgrammeData("cci", "title", "version", 2020, 2024, null, null, null, null, null, null, systemLanguages, emptyMap<String, String>())
        every { programmeDataRepository.findById(1) } returns Optional.of(existingProgrammeData)

        val programmeData = programmeDataService.get()

        assertThat(programmeData).isEqualTo(programmeDataInput)
    }

    @Test
    fun `update existing programme data`() {
        val programmeDataInput =
            InputProgrammeData("cci-updated", "title", "version", 2020, 2024, null, null, null, null, null, null, systemLanguages)
        val programmeDataUpdated =
            ProgrammeData(1, "cci-updated", "title", "version", 2020, 2024, null, null, null, null, null, null, null)
        val programmeDataExpectedOutput =
            OutputProgrammeData("cci-updated", "title", "version", 2020, 2024, null, null, null, null, null, null, systemLanguages, emptyMap<String, String>())

        every { programmeDataRepository.save(any<ProgrammeData>()) } returns programmeDataUpdated
        every { programmeDataRepository.findById(1) } returns Optional.of(existingProgrammeData)

        val programmeData = programmeDataService.update(programmeDataInput)

        assertThat(programmeData).isEqualTo(programmeDataExpectedOutput)

        val event = slot<AuditCandidate>()
        verify { auditService.logEvent(capture(event)) }
        with(event) {
            assertThat(captured.action).isEqualTo(AuditAction.PROGRAMME_BASIC_DATA_EDITED)
            assertThat(captured.description).isEqualTo("Programme basic data changed:\n" +
                "cci changed from cci to cci-updated")
        }
    }

    @Test
    fun `update existing programme data with different data`() {
        val updatedSystemLanguages = selectSystemLanguage(systemLanguages, SystemLanguage.DE)
        val programmeDataInput =
            InputProgrammeData("cci-updated", "title-updated", "version-updated", 2021, 2025,
                LocalDate.of(2020, 1, 1), LocalDate.of(2021, 2, 2),
                "d1",  LocalDate.of(2022, 3, 3),
                "d2", LocalDate.of(2022, 4, 4), updatedSystemLanguages)
        val programmeDataUpdated = programmeDataInput.toEntity(emptySet())
        val programmeDataExpectedOutput = programmeDataUpdated.toOutputProgrammeData()

        every { programmeDataRepository.save(any<ProgrammeData>()) } returns programmeDataUpdated
        every { programmeDataRepository.findById(1) } returns Optional.of(existingProgrammeData)

        val programmeData = programmeDataService.update(programmeDataInput)

        assertThat(programmeData).isEqualTo(programmeDataExpectedOutput)

        val event = slot<AuditCandidate>()
        verify { auditService.logEvent(capture(event)) }
        with(event) {
            assertThat(captured.action).isEqualTo(AuditAction.PROGRAMME_BASIC_DATA_EDITED)
            assertThat(captured.description).isEqualTo("Programme basic data changed:\n" +
                "cci changed from cci to cci-updated,\n" +
                "title changed from title to title-updated,\n" +
                "version changed from version to version-updated,\n" +
                "firstYear changed from 2020 to 2021,\n" +
                "lastYear changed from 2024 to 2025,\n" +
                "eligibleFrom changed from null to 2020-01-01,\n" +
                "eligibleUntil changed from null to 2021-02-02,\n" +
                "commissionDecisionNumber changed from null to d1,\n" +
                "commissionDecisionDate changed from null to 2022-03-03,\n" +
                "programmeAmendingDecisionNumber changed from null to d2,\n" +
                "programmeAmendingDecisionDate changed from null to 2022-04-04,\n" +
                "languagesSystem changed from EN to DE,EN")
        }
    }

    @Test
    fun `update nuts - missing programme data hardcoded row ID=1`() {
        every { programmeDataRepository.findById(eq(1)) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { programmeDataService.saveProgrammeNuts(emptySet()) }
    }

    @Test
    fun `update nuts`() {
        val regionToBeSaved = NutsRegion3(
            id = "CO011",
            title = "CO011 title",
            region2 = NutsRegion2(
                id = "CO01",
                title = "CO01 title",
                region1 = NutsRegion1(id = "CO0", title = "CO0 title", country = NutsCountry(id = "CO", title = "CO title")))
        )

        every { programmeDataRepository.findById(eq(1)) } returns Optional.of(existingProgrammeData)
        every { nutsRegion3Repository.findAllById(eq(setOf("nuts_3_id"))) } returns setOf(regionToBeSaved)
        every { programmeDataRepository.save(any<ProgrammeData>()) } returnsArgument 0

        val result = programmeDataService.saveProgrammeNuts(setOf("nuts_3_id"))
        assertThat(result.programmeNuts).isEqualTo(
            mapOf(NutsIdentifier("CO", "CO title") to
                mapOf(NutsIdentifier("CO0", "CO0 title") to
                    mapOf(
                        NutsIdentifier("CO01", "CO01 title") to
                            setOf(NutsIdentifier("CO011", "CO011 title"))
                        )
                )
            )
        )
    }

    fun selectSystemLanguage(systemLanguageSelections: List<SystemLanguageSelection>,
                             systemLanguage: SystemLanguage): List<SystemLanguageSelection> {
        // success if already selected
        if (systemLanguageSelections.any { it.name == systemLanguage.name && it.selected })
            return systemLanguageSelections

        // otherwise add
        val updatedSystemLanguageSelections: ArrayList<SystemLanguageSelection> = ArrayList()
        SystemLanguage.values().forEach {
            val selected = if (it.name == systemLanguage.name) { true } else { isSelected(systemLanguageSelections, it.name) }
            updatedSystemLanguageSelections.add(
                SystemLanguageSelection(it.name, it.translationKey, selected))
        }
        return updatedSystemLanguageSelections
    }

    private fun isSelected(systemLanguageSelections: List<SystemLanguageSelection>,
                   systemLanguage: String): Boolean {
        return systemLanguageSelections.any { it.name == systemLanguage && it.selected }
    }
}
