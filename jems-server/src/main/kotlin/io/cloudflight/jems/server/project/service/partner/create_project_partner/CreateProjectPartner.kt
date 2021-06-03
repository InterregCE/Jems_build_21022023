package io.cloudflight.jems.server.project.service.partner.create_project_partner

import io.cloudflight.jems.api.project.dto.partner.InputProjectPartnerCreate
import io.cloudflight.jems.api.project.dto.partner.OutputProjectPartnerDetail
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRole
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.authorization.CanUpdateProject
import io.cloudflight.jems.server.project.repository.partner.PartnerPersistenceProvider
import io.cloudflight.jems.server.project.repository.partner.ProjectPartnerRepository
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateProjectPartner(
    private val persistence: PartnerPersistence,
    private val projectPartnerRepository: ProjectPartnerRepository,
) : CreateProjectPartnerInteractor {

    @CanUpdateProject
    @Transactional
    @ExceptionWrapper(CreateProjectPartnerException::class)
    override fun create(projectId: Long, projectPartner: InputProjectPartnerCreate): OutputProjectPartnerDetail {
        // to be possible to list all partners for dropdowns we decided to limit total amount of them
        if (projectPartnerRepository.countByProjectId(projectId) >= PartnerPersistenceProvider.MAX_PROJECT_PARTNERS)
            throw MaximumNumberOfPartnersReached()

        // prevent multiple role LEAD_PARTNER entries
        if (projectPartner.role!!.isLead)
            validateLeadPartnerChange(projectId, projectPartner.oldLeadPartnerId)

        // prevent multiple partners with same abbreviation
        validatePartnerAbbreviationUnique(projectId, abbreviation = projectPartner.abbreviation!!)
        return persistence.create(projectId, projectPartner)
    }

    private fun validateLeadPartnerChange(projectId: Long, oldLeadPartnerId: Long?) {
        if (oldLeadPartnerId == null)
            validateOnlyOneLeadPartner(projectId)
        else
            updateOldLeadPartner(projectId, oldLeadPartnerId)
    }

    private fun validatePartnerAbbreviationUnique(projectId: Long, abbreviation: String) {
        val partnerWithSameName = projectPartnerRepository.findFirstByProjectIdAndAbbreviation(projectId, abbreviation)
        if (partnerWithSameName.isPresent) {
            throw PartnerAbbreviationNotUnique(mapOf("abbreviation" to abbreviation))
        }
    }

    /**
     * validate project partner to be saved: only one role LEAD should exist.
     */
    private fun validateOnlyOneLeadPartner(projectId: Long) {
        val projectPartner = projectPartnerRepository.findFirstByProjectIdAndRole(projectId, ProjectPartnerRole.LEAD_PARTNER)
        if (projectPartner.isPresent) {
            val currentLead = projectPartner.get()
            throw LeadPartnerAlreadyExists(mapOf("currentLeadId" to currentLead.id.toString(), "currentLeadAbbreviation" to currentLead.abbreviation))
        }
    }

    private fun updateOldLeadPartner(projectId: Long, oldLeadPartnerId: Long) {
        val oldLeadPartner = projectPartnerRepository.findFirstByProjectIdAndRole(projectId, ProjectPartnerRole.LEAD_PARTNER)
            .orElseThrow { ResourceNotFoundException("projectPartner") }

        if (oldLeadPartner.id == oldLeadPartnerId)
            projectPartnerRepository.save(oldLeadPartner.copy(role = ProjectPartnerRole.PARTNER))
        else
            throw PartnerIsNotLead()
    }
}