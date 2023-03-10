package io.cloudflight.jems.server.project.service.result.get_project_result

import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectForm
import io.cloudflight.jems.server.project.service.result.ProjectResultPersistence
import org.springframework.stereotype.Service

@Service
class GetProjectResult (
    private val projectResultPersistence: ProjectResultPersistence
) : GetProjectResultInteractor {

    @CanRetrieveProjectForm
    override fun getResultsForProject(projectId: Long, version: String?) =
        projectResultPersistence.getResultsForProject(projectId, version)

}
