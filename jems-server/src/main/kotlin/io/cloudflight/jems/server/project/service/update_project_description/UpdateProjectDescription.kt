package io.cloudflight.jems.server.project.service.update_project_description

import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanUpdateProjectForm
import io.cloudflight.jems.server.project.service.ProjectDescriptionPersistence
import io.cloudflight.jems.server.project.service.model.ProjectLongTermPlans
import io.cloudflight.jems.server.project.service.model.ProjectManagement
import io.cloudflight.jems.server.project.service.model.ProjectOverallObjective
import io.cloudflight.jems.server.project.service.model.ProjectPartnership
import io.cloudflight.jems.server.project.service.model.ProjectRelevance
import org.springframework.stereotype.Service

@Service
class UpdateProjectDescription(
    private val projectDescriptionPersistence: ProjectDescriptionPersistence,
    private val generalValidator: GeneralValidatorService
) : UpdateProjectDescriptionInteractor {

    companion object {
        const val MAX_NUMBER_OF_ITEMS = 20
    }

    @CanUpdateProjectForm
    override fun updateOverallObjective(projectId: Long, projectOverallObjective: ProjectOverallObjective): ProjectOverallObjective {
        return projectDescriptionPersistence.updateOverallObjective(projectId, projectOverallObjective)
    }

    @CanUpdateProjectForm
    override fun updateProjectRelevance(projectId: Long, projectRelevance: ProjectRelevance): ProjectRelevance {
        generalValidator.throwIfAnyIsInvalid(
            generalValidator.maxSize(projectRelevance.projectBenefits, MAX_NUMBER_OF_ITEMS, "benefits"),
            generalValidator.maxSize(projectRelevance.projectSpfRecipients, MAX_NUMBER_OF_ITEMS, "spfRecipients"),
            generalValidator.maxSize(projectRelevance.projectStrategies, MAX_NUMBER_OF_ITEMS, "strategies"),
            generalValidator.maxSize(projectRelevance.projectSynergies, MAX_NUMBER_OF_ITEMS, "synergies")
        )
        return projectDescriptionPersistence.updateProjectRelevance(projectId, projectRelevance)
    }

    @CanUpdateProjectForm
    override fun updatePartnership(projectId: Long, projectPartnership: ProjectPartnership): ProjectPartnership {
        return projectDescriptionPersistence.updatePartnership(projectId, projectPartnership)
    }

    @CanUpdateProjectForm
    override fun updateProjectManagement(projectId: Long, projectManagement: ProjectManagement): ProjectManagement {
        return projectDescriptionPersistence.updateProjectManagement(projectId, projectManagement)
    }

    @CanUpdateProjectForm
    override fun updateProjectLongTermPlans(projectId: Long, projectLongTermPlans: ProjectLongTermPlans): ProjectLongTermPlans {
        return projectDescriptionPersistence.updateProjectLongTermPlans(projectId, projectLongTermPlans)
    }
}
