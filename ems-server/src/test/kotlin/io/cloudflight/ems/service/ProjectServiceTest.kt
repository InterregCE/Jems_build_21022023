package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.InputProject
import io.cloudflight.ems.api.dto.OutputProject
import io.cloudflight.ems.api.dto.user.OutputUser
import io.cloudflight.ems.api.dto.user.OutputUserRole
import io.cloudflight.ems.entity.Account
import io.cloudflight.ems.entity.AccountRole
import io.cloudflight.ems.entity.Audit
import io.cloudflight.ems.entity.AuditAction
import io.cloudflight.ems.entity.Project
import io.cloudflight.ems.exception.ResourceNotFoundException
import io.cloudflight.ems.repository.AccountRepository
import io.cloudflight.ems.repository.ProjectRepository
import io.cloudflight.ems.security.ADMINISTRATOR
import io.cloudflight.ems.security.APPLICANT_USER
import io.cloudflight.ems.security.PROGRAMME_USER
import io.cloudflight.ems.security.model.LocalCurrentUser
import io.cloudflight.ems.security.service.SecurityService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDate
import java.util.Optional
import java.util.stream.Collectors

val TEST_DATE: LocalDate = LocalDate.now()

class ProjectServiceTest {

    private val UNPAGED = Pageable.unpaged()

    private val user = OutputUser(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        userRole = OutputUserRole(id = 1, name = "ADMIN")
    )

    private val account = Account(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        accountRole = AccountRole(id = 1, name = "ADMIN"),
        password = "hash_pass"
    )

    @MockK
    lateinit var projectRepository: ProjectRepository

    @MockK
    lateinit var accountRepository: AccountRepository

    @RelaxedMockK
    lateinit var auditService: AuditService

    @MockK
    lateinit var securityService: SecurityService

    lateinit var projectService: ProjectService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { securityService.currentUser } returns LocalCurrentUser(user, "hash_pass", emptyList())
        every { accountRepository.findById(eq(user.id!!)) } returns Optional.of(account)
        projectService = ProjectServiceImpl(projectRepository, accountRepository, auditService, securityService)
    }

    @ParameterizedTest
    @ValueSource(strings = [ADMINISTRATOR, PROGRAMME_USER])
    fun projectRetrieval_admin(role: String) {
        every { securityService.currentUser } returns
            LocalCurrentUser(user, "hash_pass", listOf(SimpleGrantedAuthority("ROLE_$role")))

        val projectToReturn = Project(
            id = 25,
            acronym = "test acronym",
            applicant = account,
            submissionDate = TEST_DATE
        )
        every { projectRepository.findAll(UNPAGED) } returns PageImpl(listOf(projectToReturn))

        // test start
        val result = projectService.findAll(UNPAGED)

        // assertions:
        assertEquals(1, result.totalElements)

        val expectedProjects = listOf(
            OutputProject(
                id = 25,
                acronym = "test acronym",
                applicant = user,
                submissionDate = TEST_DATE
            )
        )
        assertIterableEquals(expectedProjects, result.get().collect(Collectors.toList()))
    }

    @Test
    fun projectRetrieval_applicant() {
        every { securityService.currentUser } returns
                LocalCurrentUser(user, "hash_pass", listOf(SimpleGrantedAuthority("ROLE_$APPLICANT_USER")))
        every { projectRepository.findAllByApplicant_Id(eq(user.id!!), UNPAGED) } returns PageImpl(emptyList())

        assertEquals(0, projectService.findAll(UNPAGED).totalElements)
    }

    @Test
    fun projectCreation_OK() {
        val project = Project(null, "test", account, TEST_DATE)
        every { projectRepository.save(eq(project)) } returns Project(612, "test", account, TEST_DATE)

        val result = projectService.createProject(InputProject("test", TEST_DATE))

        assertEquals(result.acronym, "test")
        assertEquals(result.submissionDate, TEST_DATE)

        verifyAudit("612")
    }

    @Test
    fun projectCreation_withoutUser() {
        every { accountRepository.findById(eq(user.id!!)) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { projectService.createProject(InputProject("test", TEST_DATE)) }
    }

    @Test
    fun projectGet_OK() {
        every { projectRepository.findOneById(eq(1)) } returns
                Project(1, "test", account, TEST_DATE)

        val result = projectService.getById(1);

        assertThat(result).isNotNull()
        assertThat(result.id).isEqualTo(1);
        assertThat(result.acronym).isEqualTo("test")
        assertThat(result.submissionDate).isEqualTo(TEST_DATE);
    }

    @Test
    fun projectGet_notExisting() {
        every { projectRepository.findOneById(eq(-1)) } returns null
        assertThrows<ResourceNotFoundException> { projectService.getById(-1) }
    }

    private fun verifyAudit(projectIdExpected: String) {
        val event = slot<Audit>()

        verify { auditService.logEvent(capture(event)) }
        with(event) {
            assertEquals(projectIdExpected, captured.projectId)
            assertEquals(1, captured.user?.id)
            assertEquals("admin@admin.dev", captured.user?.email)
            assertEquals(AuditAction.PROJECT_SUBMISSION, captured.action)
        }
    }
}
