package io.cloudflight.jems.server.project.service.application.workflow.states

import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectWorkflowPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.application.workflow.ApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.InvalidPreviousStatusException
import io.cloudflight.jems.server.project.service.model.ProjectStatus
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import org.springframework.context.ApplicationEventPublisher

class InModificationApplicationState(
    override val projectSummary: ProjectSummary,
    override val projectWorkflowPersistence: ProjectWorkflowPersistence,
    override val auditPublisher: ApplicationEventPublisher,
    override val securityService: SecurityService,
    override val projectPersistence: ProjectPersistence
) : ApplicationState(projectSummary, projectWorkflowPersistence, auditPublisher, securityService, projectPersistence) {

    override fun submit() =
        ifPreviousStateIsValid().let { previousStatus ->
            if (previousStatus.status == ApplicationStatus.MODIFICATION_SUBMITTED)
                projectWorkflowPersistence.updateProjectLastResubmission(
                    projectId = projectSummary.id,
                    userId = securityService.getUserIdOrThrow(),
                    status = previousStatus
                )
            else
                projectWorkflowPersistence.updateProjectLastResubmission(
                    projectId = projectSummary.id,
                    userId = securityService.getUserIdOrThrow(),
                    status = ApplicationStatus.MODIFICATION_SUBMITTED
                )
        }

    private fun ifPreviousStateIsValid(): ProjectStatus =
        projectWorkflowPersistence.getApplicationPreviousStatus(projectSummary.id).also { previousStatus ->
            when (previousStatus.status) {
                ApplicationStatus.CONTRACTED, ApplicationStatus.MODIFICATION_SUBMITTED -> Unit
                else -> throw InvalidPreviousStatusException(
                    fromStatus = projectSummary.status,
                    toStatus = previousStatus.status
                )
            }
        }
}
