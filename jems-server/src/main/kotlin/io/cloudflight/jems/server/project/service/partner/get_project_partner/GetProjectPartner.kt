package io.cloudflight.jems.server.project.service.partner.get_project_partner

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectForm
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectPartner
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectPartnerSummaries
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerDetail
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProjectPartner(
    private val persistence: PartnerPersistence,
) : GetProjectPartnerInteractor {

    @CanRetrieveProjectForm
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetProjectPartnersByProjectIdException::class)
    override fun findAllByProjectId(projectId: Long, page: Pageable, version: String?): Page<ProjectPartnerSummary> =
        persistence.findAllByProjectId(projectId, page, version)

    @CanRetrieveProjectPartner
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetProjectPartnerByIdException::class)
    override fun getById(partnerId: Long, version: String?): ProjectPartnerDetail =
        persistence.getById(partnerId, version)

    @CanRetrieveProjectPartnerSummaries
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetProjectPartnerByProjectIdForDropdownException::class)
    override fun findAllByProjectIdForDropdown(
        projectId: Long, sort: Sort, version: String?
    ): List<ProjectPartnerSummary> =
        persistence.findAllByProjectIdForDropdown(projectId, sort, version)

    override fun findAllByProjectId(projectId: Long): Iterable<ProjectPartnerDetail> =
        persistence.findAllByProjectId(projectId)

}
