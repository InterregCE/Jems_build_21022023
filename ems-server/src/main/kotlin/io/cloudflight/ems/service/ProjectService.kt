package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.InputProject
import io.cloudflight.ems.api.dto.OutputProject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface ProjectService {

    fun getProjects(page: Pageable): Page<OutputProject>

    fun createProject(project: InputProject): OutputProject

    fun getProjectById(id: Long): Optional<OutputProject>
}
