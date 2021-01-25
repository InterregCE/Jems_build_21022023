package io.cloudflight.jems.server.factory

import io.cloudflight.jems.api.project.dto.status.ProjectApplicationStatus
import io.cloudflight.jems.api.project.dto.file.ProjectFileType
import io.cloudflight.jems.server.call.entity.CallEntity
import io.cloudflight.jems.server.user.entity.User
import io.cloudflight.jems.server.project.entity.ProjectEntity
import io.cloudflight.jems.server.project.entity.file.ProjectFile
import io.cloudflight.jems.server.project.entity.ProjectStatus
import io.cloudflight.jems.server.project.repository.ProjectFileRepository
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectStatusRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class ProjectFileFactory(
    val projectRepository: ProjectRepository,
    val projectStatusRepository: ProjectStatusRepository,
    val projectFileRepository: ProjectFileRepository
) {

    val callStart = ZonedDateTime.now().plusDays(1)
    val callEnd = ZonedDateTime.now().plusDays(20)

    @Transactional
    fun saveProject(author: User, call: CallEntity): ProjectEntity {
        val projectStatus = projectStatusRepository.save(ProjectStatus(0, null, ProjectApplicationStatus.DRAFT, author, ZonedDateTime.now(), null))
        return projectRepository.save(
            ProjectEntity(
                id = 0,
                call = call,
                acronym = "test_project",
                applicant = author,
                projectStatus = projectStatus,
                firstSubmission = projectStatus
            )
        )
    }

    @Transactional
    fun saveProjectFile(project: ProjectEntity, applicant: User): ProjectFile {
        return projectFileRepository.save(
            ProjectFile(
                0,
                "project-files",
                "project-1/cat.jpg",
                "cat.jpg",
                project,
                applicant,
                ProjectFileType.APPLICANT_FILE,
                null,
                4,
                ZonedDateTime.now()))
    }

}
