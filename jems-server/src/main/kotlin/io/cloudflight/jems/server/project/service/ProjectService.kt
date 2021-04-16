package io.cloudflight.jems.server.project.service

import io.cloudflight.jems.api.project.dto.InputProject
import io.cloudflight.jems.api.project.dto.InputProjectData
import io.cloudflight.jems.api.project.dto.ProjectDetailDTO
import io.cloudflight.jems.api.project.dto.OutputProjectSimple
import io.cloudflight.jems.server.project.dto.ProjectApplicantAndStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectService {

    fun getById(id: Long): ProjectDetailDTO

    fun getApplicantAndStatusById(id: Long): ProjectApplicantAndStatus

    /**
     * List of projects is restricted based on actual user role.
     */
    fun findAll(page: Pageable): Page<OutputProjectSimple>

    fun createProject(project: InputProject): ProjectDetailDTO

    fun update(id: Long, projectData: InputProjectData): ProjectDetailDTO

}
