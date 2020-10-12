package io.cloudflight.jems.server.factory

import io.cloudflight.jems.api.project.dto.status.ProjectApplicationStatus
import io.cloudflight.jems.server.call.entity.Call
import io.cloudflight.jems.server.project.entity.Project
import io.cloudflight.jems.server.project.entity.ProjectStatus
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectStatusRepository
import io.cloudflight.jems.server.user.entity.User
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import javax.transaction.Transactional

@Component
class ProjectFactory(
    val projectRepository: ProjectRepository,
    val projectStatusRepository: ProjectStatusRepository
) {

    @Transactional
    fun saveProject(author: User, call: Call): Project {
        val projectStatus = projectStatusRepository.save(ProjectStatus(null, null, ProjectApplicationStatus.DRAFT, author, ZonedDateTime.now(), null))
        return projectRepository.save(Project(null, call, "test_project", author, projectStatus, projectStatus))
    }


}