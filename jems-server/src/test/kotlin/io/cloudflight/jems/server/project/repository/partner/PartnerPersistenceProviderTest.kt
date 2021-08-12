package io.cloudflight.jems.server.project.repository.partner

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.EN
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.SK
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.legalstatus.ProgrammeLegalStatusEntity
import io.cloudflight.jems.server.programme.repository.legalstatus.ProgrammeLegalStatusRepository
import io.cloudflight.jems.server.project.entity.partner.PartnerIdentityRow
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import io.cloudflight.jems.server.project.entity.partner.state_aid.ProjectPartnerStateAidEntity
import io.cloudflight.jems.server.project.repository.ApplicationVersionNotFoundException
import io.cloudflight.jems.server.project.repository.ProjectNotFoundException
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionUtils
import io.cloudflight.jems.server.project.service.associatedorganization.ProjectAssociatedOrganizationService
import io.cloudflight.jems.server.project.service.model.ProjectContactType
import io.cloudflight.jems.server.project.service.model.ProjectTargetGroup
import io.cloudflight.jems.server.project.service.partner.PARTNER_ID
import io.cloudflight.jems.server.project.service.partner.PROJECT_ID
import io.cloudflight.jems.server.project.service.partner.ProjectPartnerTestUtil
import io.cloudflight.jems.server.project.service.partner.legalStatusEntity
import io.cloudflight.jems.server.project.service.partner.model.NaceGroupLevel
import io.cloudflight.jems.server.project.service.partner.model.PartnerSubType
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartner
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerContact
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerMotivation
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerVatRecovery
import io.cloudflight.jems.server.project.service.partner.projectPartner
import io.cloudflight.jems.server.project.service.partner.projectPartnerDetail
import io.cloudflight.jems.server.project.service.partner.projectPartnerEntity
import io.cloudflight.jems.server.project.service.partner.projectPartnerInclTransl
import io.cloudflight.jems.server.project.service.partner.projectPartnerSummary
import io.cloudflight.jems.server.project.service.partner.projectPartnerWithOrganizationEntity
import io.cloudflight.jems.server.project.service.partner.stateAid
import io.cloudflight.jems.server.project.service.partner.stateAidEmpty
import io.cloudflight.jems.server.project.service.partner.stateAidEntity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Optional

class PartnerPersistenceProviderTest {

    private lateinit var projectVersionUtils: ProjectVersionUtils

    @MockK
    lateinit var projectPartnerRepository: ProjectPartnerRepository

    @MockK
    lateinit var legalStatusRepo: ProgrammeLegalStatusRepository

    @MockK
    lateinit var projectRepository: ProjectRepository

    @MockK
    lateinit var projectPartnerStateAidRepository: ProjectPartnerStateAidRepository

    @MockK
    lateinit var projectAssociatedOrganizationService: ProjectAssociatedOrganizationService

    @MockK
    lateinit var projectVersionRepo: ProjectVersionRepository

    @MockK
    lateinit var partner: ProjectPartnerEntity

    lateinit var persistence: PartnerPersistenceProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        projectVersionUtils = ProjectVersionUtils(projectVersionRepo)
        persistence = PartnerPersistenceProvider(
            projectVersionUtils,
            projectPartnerRepository,
            legalStatusRepo,
            projectRepository,
            projectPartnerStateAidRepository,
            projectAssociatedOrganizationService,
        )
        //for all delete tests
        every { projectAssociatedOrganizationService.refreshSortNumbers(any()) } answers {}
    }


    @Test
    fun `should throw ResourceNotFoundException when partner does not exist`() {
        every { projectPartnerRepository.findById(-1) } returns Optional.empty()
        every { projectPartnerRepository.findById(1) } returns Optional.of(projectPartnerEntity())
        every { projectPartnerRepository.getProjectIdForPartner(-1) } throws ResourceNotFoundException("partner")
        every { projectPartnerRepository.getProjectIdForPartner(1) } returns PARTNER_ID

        assertThrows<ResourceNotFoundException> { persistence.getById(-1, null) }
        assertThat(persistence.getById(1, null)).isEqualTo(projectPartnerDetail(PARTNER_ID))
    }

    @Test
    fun `should change role of lead partner to partner in the project if it exists`() {
        val projectPartnerEntity = projectPartnerEntity()
        every { projectPartnerRepository.findFirstByProjectIdAndRole(PROJECT_ID, ProjectPartnerRole.LEAD_PARTNER) } returns Optional.of(projectPartnerEntity)
        every { projectPartnerRepository.findTop30ByProjectId(PROJECT_ID, any()) } returns listOf(projectPartnerEntity)
        every { projectPartnerRepository.saveAll(any()) } returnsArgument 0
        assertDoesNotThrow { persistence.changeRoleOfLeadPartnerToPartnerIfItExists(PROJECT_ID)}
    }

    @Test
    fun `should throw PartnerAbbreviationNotUnique when partner abbreviation alreay exists`() {
        val abbreviation = "abbreviation"
        every { projectPartnerRepository.existsByProjectIdAndAbbreviation(PROJECT_ID, abbreviation) } returns true
        assertDoesNotThrow { persistence.throwIfPartnerAbbreviationAlreadyExists(PROJECT_ID, abbreviation)}
    }


    @Test
    fun getByIdAndVersion() {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val version = "1.0"
        val mockPartnerIdentityRow: PartnerIdentityRow = mockk()
        every { mockPartnerIdentityRow.id } returns PARTNER_ID
        every { mockPartnerIdentityRow.projectId } returns PROJECT_ID
        every { mockPartnerIdentityRow.abbreviation } returns "partner"
        every { mockPartnerIdentityRow.role } returns ProjectPartnerRole.LEAD_PARTNER
        every { mockPartnerIdentityRow.sortNumber } returns 0
        every { mockPartnerIdentityRow.nameInOriginalLanguage } returns "test"
        every { mockPartnerIdentityRow.nameInEnglish } returns "test"
        every { mockPartnerIdentityRow.partnerType } returns ProjectTargetGroup.BusinessSupportOrganisation
        every { mockPartnerIdentityRow.partnerSubType } returns PartnerSubType.LARGE_ENTERPRISE
        every { mockPartnerIdentityRow.nace } returns NaceGroupLevel.A
        every { mockPartnerIdentityRow.otherIdentifierNumber } returns "12"
        every { mockPartnerIdentityRow.otherIdentifierDescription } returns null
        every { mockPartnerIdentityRow.pic } returns "009"
        every { mockPartnerIdentityRow.vat } returns "test vat"
        every { mockPartnerIdentityRow.language } returns null
        every { mockPartnerIdentityRow.department } returns null
        every { mockPartnerIdentityRow.vatRecovery } returns ProjectPartnerVatRecovery.Yes
        every { mockPartnerIdentityRow.legalStatusId } returns 1

        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(1) } returns 2
        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(-1) } returns null
        every { projectVersionRepo.findTimestampByVersion(2, "404") } returns null
        every { projectVersionRepo.findTimestampByVersion(2, version) } returns timestamp
        every { projectPartnerRepository.findPartnerAddressesByIdAsOfTimestamp(1, timestamp) } returns emptyList()
        every { projectPartnerRepository.findPartnerContactsByIdAsOfTimestamp(1, timestamp) } returns emptyList()
        every { projectPartnerRepository.findPartnerMotivationByIdAsOfTimestamp(1, timestamp) } returns emptyList()
        every { projectPartnerRepository.findPartnerIdentityByIdAsOfTimestamp(1, timestamp) } returns listOf(
            mockPartnerIdentityRow
        )

        // partner does not exist in any version
        assertThrows<ResourceNotFoundException> { persistence.getById(-1, version) }
        // no timestamp can be found for the specified partner->project
        assertThrows<ApplicationVersionNotFoundException> { persistence.getById(1, "404") }
        // historic version of partner returned (version found)
        assertThat(persistence.getById(1, version)).isEqualTo(projectPartnerDetail(address = mutableListOf()))
    }

    @Test
    fun findAllByProjectId() {
        every { projectPartnerRepository.findAllByProjectId(0, Pageable.unpaged()) } returns PageImpl(emptyList())
        every { projectPartnerRepository.findAllByProjectId(1, Pageable.unpaged()) } returns PageImpl(
            listOf(
                projectPartnerEntity()
            )
        )

        assertThat(persistence.findAllByProjectId(0, Pageable.unpaged(), null)).isEmpty()
        assertThat(persistence.findAllByProjectId(1, Pageable.unpaged(), null)).containsExactly(
            projectPartnerSummary()
        )
    }

    @Test
    fun findAllByProjectIdUnpaged() {
        every { projectPartnerRepository.findAllByProjectId(0) } returns PageImpl(emptyList())
        every { projectPartnerRepository.findAllByProjectId(1) } returns PageImpl(listOf(projectPartnerEntity()))

        assertThat(persistence.findAllByProjectId(0)).isEmpty()
        assertThat(persistence.findAllByProjectId(1)).containsExactly(projectPartnerDetail(id = PARTNER_ID))
    }

    @Test
    fun createProjectPartner() {
        val projectPartnerEntity = projectPartnerEntity()
        val projectPartnerRequest =
            ProjectPartner(0, "partner", ProjectPartnerRole.LEAD_PARTNER, legalStatusId = 1)
        val projectPartnerWithProject = ProjectPartnerEntity(
            0,
            ProjectPartnerTestUtil.project,
            projectPartnerRequest.abbreviation!!,
            projectPartnerRequest.role!!,
            legalStatus = legalStatusEntity
        )
        every { projectRepository.getReferenceIfExistsOrThrow(PROJECT_ID) } returns ProjectPartnerTestUtil.project
        every { legalStatusRepo.getReferenceIfExistsOrThrow(legalStatusEntity.id) } returns legalStatusEntity

        every { projectPartnerRepository.save(projectPartnerWithProject) } returns projectPartnerEntity
        every { projectPartnerRepository.save(projectPartnerEntity) } returns projectPartnerInclTransl
        // also handle sorting
        val projectPartners = listOf(projectPartnerEntity, projectPartnerWithProject)
        every { projectPartnerRepository.findTop30ByProjectId(1, any()) } returns projectPartners
        every { projectPartnerRepository.saveAll(any<Iterable<ProjectPartnerEntity>>()) } returnsArgument 0

        assertThat(persistence.create(PROJECT_ID, projectPartnerRequest)).isEqualTo(projectPartnerDetail())
        verify { projectPartnerRepository.save(projectPartnerWithProject) }
    }

    @Test
    fun `createProjectPartner not existing`() {

        every { projectRepository.getReferenceIfExistsOrThrow(PROJECT_ID) } throws ProjectNotFoundException()
        every { legalStatusRepo.getReferenceIfExistsOrThrow(1) } returns legalStatusEntity
        every { projectPartnerRepository.save(any()) } returns projectPartnerEntity()
        assertThrows<ProjectNotFoundException> { persistence.create(PROJECT_ID, projectPartner()) }

    }

    @Test
    fun updateProjectPartner() {
        val projectPartnerUpdate =
            projectPartner(PARTNER_ID, "updated", ProjectPartnerRole.PARTNER)
        val updatedProjectPartnerEntity =
            projectPartnerEntity(abbreviation = projectPartnerUpdate.abbreviation!!, role = projectPartnerUpdate.role!!)
        every { projectPartnerRepository.findById(PARTNER_ID) } returns Optional.of(projectPartnerEntity())
        every { projectPartnerRepository.save(updatedProjectPartnerEntity) } returns updatedProjectPartnerEntity
        every { legalStatusRepo.getReferenceIfExistsOrThrow(1) } returns legalStatusEntity

        assertThat(persistence.update(projectPartnerUpdate))
            .isEqualTo(
                projectPartnerDetail(
                    abbreviation = projectPartnerUpdate.abbreviation!!,
                    role = ProjectPartnerRole.PARTNER
                )
            )
    }

    @Test
    fun `updateProjectPartner to lead when no other leads`() {
        val projectPartnerUpdate = projectPartner(3, "updated")
        every { projectPartnerRepository.findById(3) } returns Optional.of(
            projectPartnerEntity(id = 3, role = ProjectPartnerRole.PARTNER)
        )
        every { legalStatusRepo.getReferenceIfExistsOrThrow(1) } returns legalStatusEntity

        assertThat(persistence.update(projectPartnerUpdate).role)
            .isEqualTo(ProjectPartnerRole.LEAD_PARTNER)
    }

    @Test
    fun updatePartnerContact() {
        val projectPartnerContactUpdate = ProjectPartnerContact(
            ProjectContactType.ContactPerson,
            "test",
            "test",
            "test",
            "test@ems.eu",
            "test"
        )
        val projectPartner = ProjectPartnerEntity(
            1,
            ProjectPartnerTestUtil.project,
            "updated",
            ProjectPartnerRole.PARTNER,
            legalStatus = ProgrammeLegalStatusEntity(id = 1),
            partnerType = ProjectTargetGroup.EducationTrainingCentreAndSchool
        )
        val contactPersonsEntity = setOf(projectPartnerContactUpdate.toEntity(projectPartner))
        val updatedProjectPartner = projectPartner.copy(contacts = contactPersonsEntity)

        every { projectPartnerRepository.findById(1) } returns Optional.of(projectPartner)
        every { projectPartnerRepository.save(updatedProjectPartner) } returns updatedProjectPartner
        every { legalStatusRepo.findById(1) } returns Optional.of(legalStatusEntity)

        assertThat(persistence.updatePartnerContacts(1, setOf(projectPartnerContactUpdate)))
            .isEqualTo(updatedProjectPartner.toProjectPartnerDetail())
    }

    @Test
    fun updatePartnerContact_notExisting() {
        val projectPartnerContactUpdate = ProjectPartnerContact(
            ProjectContactType.LegalRepresentative,
            "test",
            "test",
            "test",
            "test@ems.eu",
            "test"
        )
        val contactPersonsDto = setOf(projectPartnerContactUpdate)
        every { projectPartnerRepository.findById(eq(-1)) } returns Optional.empty()
        every { legalStatusRepo.findById(1) } returns Optional.of(legalStatusEntity)
        val exception = assertThrows<ResourceNotFoundException> {
            persistence.updatePartnerContacts(
                -1,
                contactPersonsDto
            )
        }
        assertThat(exception.entity).isEqualTo("projectPartner")
    }

    @Test
    fun updatePartnerMotivation() {
        val projectPartnerMotivationUpdate = ProjectPartnerMotivation(
            setOf(InputTranslation(EN, "test")),
            setOf(InputTranslation(EN, "test")),
            setOf(InputTranslation(EN, "test"))
        )
        val projectPartner = ProjectPartnerEntity(
            1,
            ProjectPartnerTestUtil.project,
            "updated",
            ProjectPartnerRole.PARTNER,
            legalStatus = ProgrammeLegalStatusEntity(id = 1),
            partnerType = ProjectTargetGroup.EducationTrainingCentreAndSchool
        )
        val updatedProjectPartner =
            projectPartner.copy(motivation = projectPartnerMotivationUpdate.toEntity(projectPartner.id))

        every { projectPartnerRepository.findById(1) } returns Optional.of(projectPartner)
        every { projectPartnerRepository.save(updatedProjectPartner) } returns updatedProjectPartner
        every { legalStatusRepo.findById(1) } returns Optional.of(legalStatusEntity)

        assertThat(persistence.updatePartnerMotivation(1, projectPartnerMotivationUpdate))
            .isEqualTo(updatedProjectPartner.toProjectPartnerDetail())
    }

    @Test
    fun updatePartnerContribution_notExisting() {
        val projectPartnerContributionUpdate = ProjectPartnerMotivation(
            setOf(InputTranslation(EN, "test")),
            setOf(InputTranslation(EN, "test")),
            setOf(InputTranslation(EN, "test"))
        )
        every { projectPartnerRepository.findById(eq(-1)) } returns Optional.empty()
        every { legalStatusRepo.findById(1) } returns Optional.of(legalStatusEntity)
        val exception = assertThrows<ResourceNotFoundException> {
            persistence.updatePartnerMotivation(
                -1,
                projectPartnerContributionUpdate
            )
        }
        assertThat(exception.entity).isEqualTo("projectPartner")
    }

    @Test
    fun createProjectPartnerWithOrganization() {
        val projectPartnerRequest = projectPartner(
            department = setOf(InputTranslation(EN, "test"))
        )
        every { projectRepository.getReferenceIfExistsOrThrow(0) } throws ProjectNotFoundException()
        every { projectRepository.getReferenceIfExistsOrThrow(PROJECT_ID) } returns ProjectPartnerTestUtil.project
        every { legalStatusRepo.getReferenceIfExistsOrThrow(legalStatusEntity.id) } returns legalStatusEntity
        every { projectPartnerRepository.save(any()) } returns projectPartnerWithOrganizationEntity
        // also handle sorting
        val projectPartners = listOf(projectPartnerEntity(), projectPartnerWithOrganizationEntity)
        every { projectPartnerRepository.findTop30ByProjectId(PROJECT_ID, any()) } returns projectPartners
        every { projectPartnerRepository.saveAll(any<Iterable<ProjectPartnerEntity>>()) } returnsArgument 0

        assertThrows<ProjectNotFoundException> { persistence.create(0, projectPartnerRequest) }
        assertThat(
            persistence.create(PROJECT_ID, projectPartnerRequest)
        ).isEqualTo(projectPartnerDetail(department = setOf(InputTranslation(EN, "test"))))
    }

    @Test
    fun updateProjectPartnerWithOrganization() {
        val projectPartnerUpdate = projectPartner(
            abbreviation = "updated",
            role = ProjectPartnerRole.PARTNER,
            department = setOf(InputTranslation(EN, "test"))
        )
        every { projectPartnerRepository.findById(PARTNER_ID) } returns Optional.of(projectPartnerEntity())
        every { legalStatusRepo.getReferenceIfExistsOrThrow(legalStatusEntity.id) } returns legalStatusEntity
        every { projectRepository.getReferenceIfExistsOrThrow(PROJECT_ID) } returns ProjectPartnerTestUtil.project

        assertThat(persistence.update(projectPartnerUpdate))
            .isEqualTo(
                projectPartnerDetail(
                    abbreviation = projectPartnerUpdate.abbreviation!!,
                    role = ProjectPartnerRole.PARTNER,
                    department = setOf(InputTranslation(EN, "test"))
                )
            )
    }

    @Test
    fun deleteProjectPartnerWithOrganization() {
        val projectPartnerWithOrganization = ProjectPartnerEntity(
            id = 1,
            project = ProjectPartnerTestUtil.project,
            abbreviation = "partner",
            role = ProjectPartnerRole.LEAD_PARTNER,
            nameInOriginalLanguage = projectPartnerWithOrganizationEntity.nameInOriginalLanguage,
            nameInEnglish = projectPartnerWithOrganizationEntity.nameInEnglish,
            translatedValues = projectPartnerWithOrganizationEntity.translatedValues,
            legalStatus = ProgrammeLegalStatusEntity(id = 1)
        )
        every { projectPartnerRepository.findById(projectPartnerWithOrganization.id) } returns Optional.of(
            projectPartnerWithOrganization
        )
        every { projectPartnerRepository.deleteById(projectPartnerWithOrganization.id) } returns Unit
        every { projectPartnerRepository.findTop30ByProjectId(ProjectPartnerTestUtil.project.id, any()) } returns emptySet()
        every { projectPartnerRepository.saveAll(emptyList()) } returns emptyList()

        assertDoesNotThrow { persistence.deletePartner(projectPartnerWithOrganization.id) }
        verify { projectAssociatedOrganizationService.refreshSortNumbers(ProjectPartnerTestUtil.project.id) }
    }

    @Test
    fun deleteProjectPartnerWithoutOrganization() {
        every { projectPartnerRepository.findById(PARTNER_ID) } returns Optional.of(projectPartnerEntity())
        every { projectPartnerRepository.deleteById(PARTNER_ID) } returns Unit
        every { projectPartnerRepository.findTop30ByProjectId(ProjectPartnerTestUtil.project.id, any()) } returns emptySet()
        every { projectPartnerRepository.saveAll(emptyList()) } returns emptyList()

        assertDoesNotThrow { persistence.deletePartner(PARTNER_ID) }
        verify { projectAssociatedOrganizationService.refreshSortNumbers(ProjectPartnerTestUtil.project.id) }
    }

    @Test
    fun deleteProjectPartner_notExisting() {
        every { projectPartnerRepository.findById(-1) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { persistence.deletePartner(-1) }
    }

    @Test
    fun `get ProjectId for Partner`() {
        val entity: ProjectPartnerEntity = mockk()
        every { entity.project.id } returns 2
        every { projectPartnerRepository.findById(1) } returns Optional.of(entity)
        assertThat(persistence.getProjectIdForPartnerId(1)).isEqualTo(2)
    }

    @Test
    fun `get ProjectId for Partner - not existing`() {
        every { projectPartnerRepository.findById(1) } returns Optional.empty()
        val ex = assertThrows<ResourceNotFoundException> { persistence.getProjectIdForPartnerId(1) }
        assertThat(ex.entity).isEqualTo("projectPartner")
    }

    @Test
    fun `get ProjectId for historic Partner`() {
        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(1) } returns 2
        assertThat(persistence.getProjectIdForPartnerId(1, "1.0")).isEqualTo(2)
    }

    @Test
    fun `get ProjectId for historic Partner - not existing`() {
        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(PARTNER_ID) } returns null
        assertThrows<ResourceNotFoundException> { persistence.getProjectIdForPartnerId(PARTNER_ID, "404") }
    }

    @Test
    fun `get state aid`() {
        every { partner.project.id } returns 25L
        every { projectPartnerRepository.findById(PARTNER_ID) } returns Optional.of(partner)
        every { projectPartnerStateAidRepository.findById(PARTNER_ID) } returns Optional.of(stateAidEntity)

        assertThat(persistence.getPartnerStateAid(PARTNER_ID, null))
            .isEqualTo(stateAid)
    }

    @Test
    fun `get state aid - not existing`() {
        every { partner.project.id } returns 95L
        every { projectPartnerRepository.findById(PARTNER_ID) } returns Optional.of(partner)
        every { projectPartnerStateAidRepository.findById(PARTNER_ID) } returns Optional.empty()

        assertThat(persistence.getPartnerStateAid(PARTNER_ID, null))
            .isEqualTo(stateAidEmpty)
    }

    @Test
    fun `get state aid - historical`() {
        val version = "some historical version"
        val timestamp = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime())

        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(PARTNER_ID) } returns 909L
        every { projectVersionRepo.findTimestampByVersion(909L, version) } returns timestamp
        every { projectPartnerStateAidRepository.findPartnerStateAidByIdAsOfTimestamp(PARTNER_ID, timestamp) } returns listOf(
            PartnerStateAidRowTest(EN, PARTNER_ID, answer1 = true, answer2 = false, justification1 = "Is true"),
            PartnerStateAidRowTest(SK, PARTNER_ID, answer1 = true, answer2 = false, justification2 = "Is false"),
        )

        assertThat(persistence.getPartnerStateAid(PARTNER_ID, version))
            .isEqualTo(stateAid)
    }

    @Test
    fun `get state aid - historical but not existing`() {
        val version = "some historical version"
        val timestamp = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime())

        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(PARTNER_ID) } returns 1029L
        every { projectVersionRepo.findTimestampByVersion(1029L, version) } returns timestamp
        every { projectPartnerStateAidRepository.findPartnerStateAidByIdAsOfTimestamp(PARTNER_ID, timestamp) } returns emptyList()

        assertThat(persistence.getPartnerStateAid(PARTNER_ID, version))
            .isEqualTo(stateAidEmpty)
    }

    @Test
    fun `update state aid - historical but not existing`() {
        val stateAidEnitySlot = slot<ProjectPartnerStateAidEntity>()
        every { projectPartnerStateAidRepository.save(capture(stateAidEnitySlot)) } returnsArgument 0

        assertThat(persistence.updatePartnerStateAid(PARTNER_ID, stateAid)).isEqualTo(stateAid)

        assertThat(stateAidEnitySlot.captured.partnerId).isEqualTo(PARTNER_ID)
        assertThat(stateAidEnitySlot.captured.answer1).isTrue
        assertThat(stateAidEnitySlot.captured.answer2).isFalse
        assertThat(stateAidEnitySlot.captured.answer3).isNull()
        assertThat(stateAidEnitySlot.captured.answer4).isNull()
        assertThat(stateAidEnitySlot.captured.translatedValues).hasSize(2)
    }

    @Test
    fun `should throw PartnerNotFoundInProjectException when partner does not exist in the project`() {
        every { projectPartnerRepository.existsByProjectIdAndId(PROJECT_ID, PARTNER_ID) } returns false
        assertThrows<PartnerNotFoundInProjectException> {
            (persistence.throwIfNotExistsInProject(PROJECT_ID, PARTNER_ID))
        }
    }

    @Test
    fun `should return Unit when partner exists in the project`() {
        every { projectPartnerRepository.existsByProjectIdAndId(PROJECT_ID, PARTNER_ID) } returns true
        assertThat(persistence.throwIfNotExistsInProject(PROJECT_ID, PARTNER_ID)).isEqualTo(Unit)
    }
}
