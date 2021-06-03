package io.cloudflight.jems.server.project.service.associatedorganization


import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganization
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationDetail
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.repository.ProjectAssociatedOrganizationRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionUtils
import io.cloudflight.jems.server.project.repository.partner.ProjectPartnerRepository
import io.cloudflight.jems.server.project.repository.partner.toOutputProjectPartnerHistoricalData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Repository
class AssociatedOrganizationPersistenceProvider(
    private val projectPartnerRepo: ProjectPartnerRepository,
    private val projectAssociatedOrganizationRepo: ProjectAssociatedOrganizationRepository,
    private val projectVersionUtils: ProjectVersionUtils,
) : AssociatedOrganizationPersistence {
    @Transactional(readOnly = true)

    override fun getById(projectId: Long, id: Long, version: String?): OutputProjectAssociatedOrganizationDetail? {
        return projectVersionUtils.fetch(version, projectId,
            currentVersionFetcher = {
                projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, id)
                    .map { it.toOutputProjectAssociatedOrganizationDetail() }
                    .orElseThrow { ResourceNotFoundException("projectAssociatedOrganisation") }
            },
            previousVersionFetcher = { timestamp ->
                getAssociatedOrganizationHistoricalDetail(id, projectId, timestamp)
            }
        )
    }

    @Transactional(readOnly = true)
    override fun findAllByProjectId(
        projectId: Long,
        page: Pageable,
        version: String?
    ): Page<OutputProjectAssociatedOrganization> {
        return projectVersionUtils.fetch(version, projectId,
            currentVersionFetcher = {
                projectAssociatedOrganizationRepo.findAllByProjectId(projectId, page)
                    .map { it.toOutputProjectAssociatedOrganization() }
            },
            previousVersionFetcher = { timestamp ->
                projectAssociatedOrganizationRepo.findAllByProjectId(projectId, page, timestamp)
                    .map { it.toOutputAssociatedOrganizationHistoricalData() }
            }
        ) ?: Page.empty()
    }

    private fun getAssociatedOrganizationHistoricalDetail(
        id: Long,
        projectId: Long,
        timestamp: Timestamp,
    ): OutputProjectAssociatedOrganizationDetail {
        val partnerId = projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, id)
            .map { it.toOutputProjectAssociatedOrganizationDetail() }
            .orElseThrow { ResourceNotFoundException("projectAssociatedOrganisation") }.partner.id
        val partner = projectPartnerRepo.findOneByIdAsOfTimestamp(partnerId!!, timestamp)
            .toOutputProjectPartnerHistoricalData()
        val address =
            projectAssociatedOrganizationRepo.findAssociatedOrganizationAddressesByIdAsOfTimestamp(id, timestamp)
                .toProjectAssociatedOrganizationAddressHistoricalData().firstOrNull()
        val contacts =
            projectAssociatedOrganizationRepo.findAssociatedOrganizationContactsByIdAsOfTimestamp(id, timestamp)
                .toAssociatedOrganizationContactHistoricalData()
        return projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, id, timestamp)
            .toAssociatedOrganizationDetailHistoricalData(partner, address, contacts).first()
    }
}