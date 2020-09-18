package io.cloudflight.ems.project.controller

import io.cloudflight.ems.api.project.ProjectDescriptionApi
import io.cloudflight.ems.api.project.dto.description.InputProjectLongTermPlans
import io.cloudflight.ems.api.project.dto.description.InputProjectManagement
import io.cloudflight.ems.api.project.dto.description.InputProjectOverallObjective
import io.cloudflight.ems.api.project.dto.description.InputProjectPartnership
import io.cloudflight.ems.api.project.dto.description.InputProjectRelevance
import io.cloudflight.ems.api.project.dto.description.OutputProjectDescription
import io.cloudflight.ems.api.project.dto.description.OutputProjectLongTermPlans
import io.cloudflight.ems.api.project.dto.description.OutputProjectManagement
import io.cloudflight.ems.project.service.ProjectDescriptionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectDescriptionController(
    private val projectDescriptionService: ProjectDescriptionService
) : ProjectDescriptionApi {

    @PreAuthorize("@projectAuthorization.canReadProject(#id)")
    override fun getProjectDescription(id: Long): OutputProjectDescription {
        return projectDescriptionService.getProjectDescription(id)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#id)")
    override fun updateProjectOverallObjective(id: Long, overallObjective: InputProjectOverallObjective): String? {
        return projectDescriptionService.updateOverallObjective(id, overallObjective.overallObjective)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#id)")
    override fun updateProjectRelevance(id: Long, project: InputProjectRelevance): InputProjectRelevance {
        return projectDescriptionService.updateProjectRelevance(id, project)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#id)")
    override fun updateProjectPartnership(id: Long, partnership: InputProjectPartnership): String? {
        return projectDescriptionService.updatePartnership(id, partnership.partnership)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#id)")
    override fun updateProjectManagement(id: Long, project: InputProjectManagement): OutputProjectManagement {
        return projectDescriptionService.updateProjectManagement(id, project)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#id)")
    override fun updateProjectLongTermPlans(id: Long, project: InputProjectLongTermPlans): OutputProjectLongTermPlans {
        return projectDescriptionService.updateProjectLongTermPlans(id, project)
    }

}