package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.InputProject
import io.cloudflight.ems.api.dto.OutputProject
import io.cloudflight.ems.exception.DataValidationException
import io.cloudflight.ems.service.ProjectDtoUtilClass.Companion.getDtoFrom
import io.cloudflight.ems.service.ProjectDtoUtilClass.Companion.toEntity
import io.cloudflight.ems.repository.ProjectRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProjectServiceImpl(
    private val projectRepo: ProjectRepository
) : ProjectService {

    @Transactional(readOnly = true)
    override fun getProjects(page: Pageable): Page<OutputProject> {
        return projectRepo.findAll(page).map { getDtoFrom(it) }
    }

    @Transactional
    override fun createProject(project: InputProject): OutputProject {
        validateProject(project)
        val createdProject = projectRepo.save(toEntity(project))
        return getDtoFrom(createdProject)
    }

    private fun validateProject(project: InputProject) {
        val validationErrors: MutableMap<String, List<String>> = mutableMapOf()

        if (project.acronym.isNullOrBlank()) {
            validationErrors.put("acronym", listOf(DataValidationException.NULL))
        } else if (project.acronym!!.length > 25) {
            validationErrors.put("acronym", listOf(DataValidationException.STRING_LONG))
        }

        if (project.submissionDate == null) {
            validationErrors.put("submissionDate", listOf(DataValidationException.NULL))
        } else if (project.submissionDate!!.isBefore(LocalDate.now())) {
            validationErrors.put("submissionDate", listOf(DataValidationException.LOCAL_DATE_IN_PAST))
        }

        if (validationErrors.isNotEmpty()) {
            throw DataValidationException(validationErrors.toMap())
        }
    }

}
