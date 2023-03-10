package io.cloudflight.jems.server.project.authorization

import io.cloudflight.jems.server.authentication.authorization.Authorization
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
// we need ProjectFormRetrieve here because otherwise user without ProjectRetrieve will
// not be able to see other project details than form
@PreAuthorize("@projectAuthorization.hasPermission('ProjectRetrieve') || @projectAuthorization.hasPermission('ProjectFormRetrieve', #projectId) || @projectAuthorization.isUserViewCollaboratorForProjectOrThrow(#projectId)")
annotation class CanRetrieveProject

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.hasPermission('ProjectRetrieve') || @projectAuthorization.hasPermission('ProjectFormRetrieve', #projectId) || @projectAuthorization.isUserViewCollaboratorForProjectOrThrow(#projectId)")
annotation class CanRetrieveProjectVersion

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.hasPermission('ProjectRetrieve')")
annotation class CanRetrieveProjects

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectsWithOwnershipRetrieve')")
annotation class CanRetrieveProjectsWithOwnership

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectCreate')")
annotation class CanCreateProject

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.hasPermission('ProjectFormRetrieve', #projectId) || @projectAuthorization.isUserViewCollaboratorForProjectOrThrow(#projectId)")
annotation class CanRetrieveProjectForm

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.canUpdateProject(#projectId, false)")
annotation class CanUpdateProjectForm

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.canUpdateProject(#projectId, true)")
annotation class CanUpdateProjectFormOnlyBeforeContracted

@Component
class ProjectAuthorization(
    override val securityService: SecurityService,
    val projectPersistence: ProjectPersistence,
) : Authorization(securityService) {

    fun isUserViewCollaboratorForProjectOrThrow(projectId: Long): Boolean {
        val project = projectPersistence.getApplicantAndStatusById(projectId)
        val isOwner = isActiveUserIdEqualToOneOf(project.getUserIdsWithViewLevel())
        if (isOwner)
            return true
        throw ResourceNotFoundException("project") // should be same exception as if entity not found
    }

    fun canUpdateProject(projectId: Long, onlyBeforeContracted: Boolean): Boolean {
        val project = projectPersistence.getApplicantAndStatusById(projectId)
        val canSeeProject = hasPermission(UserRolePermission.ProjectFormUpdate, projectId)
            || isActiveUserIdEqualToOneOf(project.getUserIdsWithEditLevel())
        if (canSeeProject) {
            return if (onlyBeforeContracted)
                project.projectStatus.isModifiableStatusBeforeApproved() || project.projectStatus == ApplicationStatus.MODIFICATION_PRECONTRACTING
            else
                project.projectStatus.canBeModified()
        }
        throw ResourceNotFoundException("project") // should be same exception as if entity not found
    }

}
