package io.cloudflight.jems.server.factory

import io.cloudflight.jems.server.call.entity.CallEntity
import io.cloudflight.jems.server.project.entity.ProjectEntity
import io.cloudflight.jems.server.project.entity.ProjectStatusHistoryEntity
import io.cloudflight.jems.server.project.entity.file.ProjectFileCategoryEntity
import io.cloudflight.jems.server.project.entity.file.ProjectFileCategoryId
import io.cloudflight.jems.server.project.entity.file.ProjectFileEntity
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectStatusHistoryRepository
import io.cloudflight.jems.server.project.repository.file.ProjectFileCategoryRepository
import io.cloudflight.jems.server.project.repository.file.ProjectFileRepository
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.file.model.ProjectFileCategoryType
import io.cloudflight.jems.server.user.entity.UserEntity
import io.cloudflight.jems.server.utils.PARTNER_ID
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class ProjectFileFactory(
    val projectRepository: ProjectRepository,
    val projectStatusHistoryRepository: ProjectStatusHistoryRepository,
    val projectFileRepository: ProjectFileRepository,
    val projectFileCategoryRepository: ProjectFileCategoryRepository
) {

    val callStart = ZonedDateTime.now().plusDays(1)
    val callEnd = ZonedDateTime.now().plusDays(20)

    var counter = 100L

    @Transactional
    fun saveProject(author: UserEntity, call: CallEntity): ProjectEntity {
        counter++
        val projectStatus = projectStatusHistoryRepository.save(
            ProjectStatusHistoryEntity(
                0,
                null,
                ApplicationStatus.DRAFT,
                author,
                ZonedDateTime.now(),
                null
            )
        )
        return projectRepository.save(
            ProjectEntity(
                id = 0,
                customIdentifier = "$counter",
                call = call,
                acronym = "test_project",
                applicant = author,
                currentStatus = projectStatus,
                firstSubmission = projectStatus,
            )
        )
    }

    @Transactional
    fun saveProjectFile(project: ProjectEntity, applicant: UserEntity): ProjectFileEntity {
        return projectFileRepository.save(
            ProjectFileEntity(
                0,
                "cat.jpg",
                project,
                applicant,
                "project-1/cat.jpg",
                4,
                ZonedDateTime.now()
            )
        ).also {
            projectFileCategoryRepository.saveAll(
                listOf(
                    ProjectFileCategoryEntity(
                        ProjectFileCategoryId(it.id, ProjectFileCategoryType.APPLICATION.name),
                        it
                    ),
                    ProjectFileCategoryEntity(
                        ProjectFileCategoryId(it.id, ProjectFileCategoryType.PARTNER.name),
                        it
                    ),
                    ProjectFileCategoryEntity(
                        ProjectFileCategoryId(it.id, "${ProjectFileCategoryType.PARTNER.name}=$PARTNER_ID"),
                        it
                    )
                )
            )
        }
    }

}
