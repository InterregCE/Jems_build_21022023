package io.cloudflight.jems.server.project.authorization

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.authentication.model.LocalCurrentUser
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.applicantUser
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.userApplicant
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.model.ProjectApplicantAndStatus
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectCheckApplicationForm
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectRetrieve
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectSubmission
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority

internal class ProjectStatusAuthorizationTest: UnitTest() {

    companion object {
        private const val PROJECT_ID = 588L
    }

    @MockK
    lateinit var securityService: SecurityService

    @MockK
    lateinit var projectPersistence: ProjectPersistence

    @MockK
    lateinit var mockStatus: ApplicationStatus

    @MockK
    lateinit var authorizationUtilService: AuthorizationUtilService

    @InjectMockKs
    lateinit var projectStatusAuthorization: ProjectStatusAuthorization

    @BeforeEach
    fun resetMocks() {
        clearMocks(securityService)
    }

    @Test
    fun `owner can submit`() {
        val user = applicantUser

        every { projectPersistence.getApplicantAndStatusById(PROJECT_ID) } returns
            ProjectApplicantAndStatus(PROJECT_ID,
                applicantId = user.user.id,
                projectStatus = mockStatus,
                collaboratorManageIds = emptySet(),
                collaboratorEditIds = setOf(user.user.id),
                collaboratorViewIds = emptySet(),
            )
        every { securityService.getUserIdOrThrow() } returns user.user.id

        // verify test setup
        assertThat(user.user.assignedProjects).isEmpty()
        assertThat(user.authorities).doesNotContain(SimpleGrantedAuthority(ProjectSubmission.name))

        assertThat(projectStatusAuthorization.hasPermissionOrIsEditCollaborator(ProjectSubmission, PROJECT_ID)).isTrue
    }

    @Test
    fun `user NOT owner, has permission for THIS PROJECT, can submit`() {
        val user = LocalCurrentUser(userApplicant.copy(assignedProjects = setOf(PROJECT_ID)), "hash_pass", applicantUser.authorities union setOf(SimpleGrantedAuthority(ProjectCheckApplicationForm.name)))

        every { projectPersistence.getApplicantAndStatusById(PROJECT_ID) } returns
            ProjectApplicantAndStatus(PROJECT_ID,
                applicantId = 3552L,
                projectStatus = mockStatus,
                collaboratorManageIds = emptySet(),
                collaboratorEditIds = emptySet(),
                collaboratorViewIds = emptySet(),
            )
        every { securityService.currentUser } returns user
        every { securityService.getUserIdOrThrow() } returns user.user.id

        // verify test setup
        assertThat(user.user.assignedProjects).contains(PROJECT_ID)
        assertThat(user.authorities).contains(SimpleGrantedAuthority(ProjectCheckApplicationForm.name))

        assertThat(projectStatusAuthorization.hasPermissionOrIsEditCollaborator(ProjectCheckApplicationForm, PROJECT_ID)).isTrue
    }

    @Test
    fun `user NOT owner, without permission, cannot submit, he does NOT have Retrieve also`() {
        val user = applicantUser

        every { projectPersistence.getApplicantAndStatusById(PROJECT_ID) } returns
            ProjectApplicantAndStatus(PROJECT_ID,
                applicantId = 3482L,
                projectStatus = mockStatus,
                collaboratorManageIds = emptySet(),
                collaboratorEditIds = emptySet(),
                collaboratorViewIds = emptySet(),
            )
        every { securityService.currentUser } returns user
        every { securityService.getUserIdOrThrow() } returns user.user.id

        // verify test setup
        assertThat(user.user.assignedProjects).isEmpty()
        assertThat(user.authorities).doesNotContain(SimpleGrantedAuthority(ProjectRetrieve.name))

        assertThrows<ResourceNotFoundException> { projectStatusAuthorization.hasPermissionOrIsEditCollaborator(ProjectCheckApplicationForm, PROJECT_ID) }
    }

    @Test
    fun `user is controller with permission`() {
        every {
            authorizationUtilService.hasPermissionAsController(ProjectCheckApplicationForm, PROJECT_ID)
        } returns true
        val user = applicantUser
        every { securityService.currentUser } returns user
        assertThat(user.user.assignedProjects).isEmpty()

        assertThat(projectStatusAuthorization.hasPermissionOrAsController(ProjectCheckApplicationForm, PROJECT_ID))
            .isTrue
    }

    @Test
    fun `user is controller without permission`() {
        every {
            authorizationUtilService.hasPermissionAsController(ProjectCheckApplicationForm, PROJECT_ID)
        } returns false
        val user = applicantUser
        every { securityService.currentUser } returns user
        assertThat(user.user.assignedProjects).isEmpty()

        assertThat(projectStatusAuthorization.hasPermissionOrAsController(ProjectCheckApplicationForm, PROJECT_ID))
            .isFalse
    }
}
