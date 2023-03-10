package io.cloudflight.jems.server.project.service.associatedorganization

import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganization
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AssociatedOrganizationPersistence {
    fun getById(projectId: Long, id: Long, version: String? = null): OutputProjectAssociatedOrganizationDetail?

    fun findAllByProjectId(
        projectId: Long,
        page: Pageable,
        version: String? = null
    ): Page<OutputProjectAssociatedOrganization>

    fun findAllByProjectId(projectId: Long,  version: String? = null): List<OutputProjectAssociatedOrganizationDetail>

    fun deactivate(id:Long)
}
