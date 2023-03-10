package io.cloudflight.jems.server.project.service.associatedorganization.get_associated_organization

import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganization
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationDetail
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectForm
import io.cloudflight.jems.server.project.service.associatedorganization.AssociatedOrganizationPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAssociatedOrganization(
    private val persistence: AssociatedOrganizationPersistence,
) : GetAssociatedOrganizationInteractor {

    @Transactional(readOnly = true)
    @CanRetrieveProjectForm
    @ExceptionWrapper(GetAssociatedOrganizationByIdException::class)
    override fun getById(projectId: Long, id: Long, version: String?): OutputProjectAssociatedOrganizationDetail? =
        persistence.getById(projectId, id, version)

    @Transactional(readOnly = true)
    @CanRetrieveProjectForm
    @ExceptionWrapper(GetAssociatedOrganizationsByProjectIdException::class)
    override fun findAllByProjectId(
        projectId: Long,
        page: Pageable,
        version: String?
    ): Page<OutputProjectAssociatedOrganization> =
        persistence.findAllByProjectId(projectId, page, version)
}
