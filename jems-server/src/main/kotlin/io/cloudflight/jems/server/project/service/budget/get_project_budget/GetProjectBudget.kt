package io.cloudflight.jems.server.project.service.budget.get_project_budget

import io.cloudflight.jems.server.project.authorization.CanReadProject
import io.cloudflight.jems.server.project.service.budget.ProjectBudgetPersistence
import io.cloudflight.jems.server.project.service.budget.model.PartnerBudget
import io.cloudflight.jems.server.project.service.budget.model.ProjectPartnerCost
import io.cloudflight.jems.server.project.service.lumpsum.ProjectLumpSumPersistence
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetOptionsPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class GetProjectBudget(
    private val persistence: ProjectBudgetPersistence,
    private val optionPersistence: ProjectPartnerBudgetOptionsPersistence,
    private val lumpSumPersistence: ProjectLumpSumPersistence,
) : GetProjectBudgetInteractor {

    @Transactional(readOnly = true)
    @CanReadProject
    override fun getBudget(projectId: Long): List<PartnerBudget> {
        val partners = persistence.getPartnersForProjectId(projectId = projectId).associateBy { it.id!! }

        val options = optionPersistence.getBudgetOptions(partners.keys).iterator().asSequence().associateBy { it.partnerId }
        val staffPerPartner = persistence.getStaffCosts(partners.keys).groupByPartnerId()
        val travelPerPartner = persistence.getTravelCosts(partners.keys).groupByPartnerId()
        val externalPerPartner = persistence.getExternalCosts(partners.keys).groupByPartnerId()
        val equipmentPerPartner = persistence.getEquipmentCosts(partners.keys).groupByPartnerId()
        val infrastructurePerPartner = persistence.getInfrastructureCosts(partners.keys).groupByPartnerId()

        val lumpSumIds = lumpSumPersistence.getLumpSums(projectId).mapTo(HashSet()) { it.id!! }
        val lumpSumContributionPerPartner = persistence.getLumpSumContributionPerPartner(lumpSumIds)

        return partners.map { (partnerId, partner) ->
            PartnerBudget(
                partner = partner,
                staffCostsFlatRate = options[partnerId]?.staffCostsFlatRate,
                officeAndAdministrationOnStaffCostsFlatRate = options[partnerId]?.officeAndAdministrationOnStaffCostsFlatRate,
                travelAndAccommodationOnStaffCostsFlatRate = options[partnerId]?.travelAndAccommodationOnStaffCostsFlatRate,
                otherCostsOnStaffCostsFlatRate = options[partnerId]?.otherCostsOnStaffCostsFlatRate,
                staffCosts = staffPerPartner[partnerId] ?: BigDecimal.ZERO,
                travelCosts = travelPerPartner[partnerId] ?: BigDecimal.ZERO,
                externalCosts = externalPerPartner[partnerId] ?: BigDecimal.ZERO,
                equipmentCosts = equipmentPerPartner[partnerId] ?: BigDecimal.ZERO,
                infrastructureCosts = infrastructurePerPartner[partnerId] ?: BigDecimal.ZERO,
                lumpSumContribution = lumpSumContributionPerPartner[partnerId] ?: BigDecimal.ZERO,
            )
        }
    }

    private fun Collection<ProjectPartnerCost>.groupByPartnerId() = associateBy({ it.partnerId }, { it.sum })

}
