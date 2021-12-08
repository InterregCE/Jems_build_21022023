package io.cloudflight.jems.server.project.controller

import io.cloudflight.jems.api.project.dto.assignment.CollaboratorLevelDTO
import io.cloudflight.jems.api.project.dto.assignment.ProjectUserCollaboratorDTO
import io.cloudflight.jems.api.project.dto.assignment.UpdateProjectUserCollaboratorDTO
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.user.entity.CollaboratorLevel
import io.cloudflight.jems.server.user.service.model.assignment.CollaboratorAssignedToProject
import io.cloudflight.jems.server.user.service.userproject.assign_user_collaborator_to_project.AssignUserCollaboratorToProjectInteractor
import io.cloudflight.jems.server.user.service.userproject.get_user_collaborators_assigned_to_projects.GetUserCollaboratorsAssignedToProjectsInteractor
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProjectUserCollaboratorControllerTest : UnitTest() {

    @MockK
    lateinit var assignUserCollaboratorToProject: AssignUserCollaboratorToProjectInteractor

    @MockK
    lateinit var getUserCollaboratorsAssignedToProjects: GetUserCollaboratorsAssignedToProjectsInteractor

    @InjectMockKs
    lateinit var controller: ProjectUserCollaboratorController

    @BeforeEach
    fun resetMocks() {
        clearMocks(assignUserCollaboratorToProject)
    }

    @Test
    fun listProjectsWithAssignedUsers() {
        every { getUserCollaboratorsAssignedToProjects.getUserIdsForProject(14L) } returns listOf(
            CollaboratorAssignedToProject(
                userId = 10L,
                userEmail = "email",
                level = CollaboratorLevel.VIEW,
            )
        )
        assertThat(controller.listAssignedUserCollaborators(14L)).containsExactly(
            ProjectUserCollaboratorDTO(
                userId = 10L,
                userEmail = "email",
                level = CollaboratorLevelDTO.VIEW,
            )
        )
    }

    @Test
    fun assignUserToProjectInteractor() {
        val dataSlot = slot<Set<Pair<String, CollaboratorLevel>>>()
        every { assignUserCollaboratorToProject.updateUserAssignmentsOnProject(60L, capture(dataSlot)) } returns emptyList()

        controller.updateAssignedUserCollaborators(
            projectId = 60L,
            users = setOf(
                UpdateProjectUserCollaboratorDTO(
                    userEmail = "email",
                    level = CollaboratorLevelDTO.MANAGE,
                ),
            ),
        )

        assertThat(dataSlot.captured).containsExactly(
            Pair("email", CollaboratorLevel.MANAGE)
        )
    }

}
