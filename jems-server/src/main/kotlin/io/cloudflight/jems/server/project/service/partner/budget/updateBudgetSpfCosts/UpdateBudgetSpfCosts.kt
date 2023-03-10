package io.cloudflight.jems.server.project.service.partner.budget.updateBudgetSpfCosts

import io.cloudflight.jems.server.project.authorization.CanUpdateProjectPartner
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.budget.BudgetCostValidator
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetCostsUpdatePersistence
import io.cloudflight.jems.server.project.service.partner.budget.truncate
import io.cloudflight.jems.server.project.service.partner.model.BudgetSpfCostEntry
import io.cloudflight.jems.server.project.service.partner.model.truncateBaseEntryNumbers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateBudgetSpfCosts(
    private val persistence: ProjectPartnerBudgetCostsUpdatePersistence,
    private val projectPersistence: ProjectPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val budgetCostValidator: BudgetCostValidator
) : UpdateBudgetSpfCostsInteractor {

    @Transactional
    @CanUpdateProjectPartner
    override fun updateBudgetSpfCosts(
        partnerId: Long,
        spfCosts: List<BudgetSpfCostEntry>
    ): List<BudgetSpfCostEntry> {
        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
        val callId = projectPersistence.getCallIdOfProject(projectId)
        val periods = spfCosts.map { it.budgetPeriods }.flatten().toSet()
        budgetCostValidator.validateAgainstAFConfig(
            callId,
            periods,
            null,
            spfCosts.map { it.numberOfUnits },
            spfCosts.map { Pair(it.unitCostId, it.unitType) }
        )

        budgetCostValidator.validateBaseEntries(spfCosts)
        budgetCostValidator.validatePricePerUnits(spfCosts.map { it.pricePerUnit })
        // validate no unit costs allowed at SPF
        budgetCostValidator.validateAllowedUnitCosts(
            availableUnitCosts = emptyList(),
            spfCosts.filter { it.unitCostId != null }
                .map { BudgetCostValidator.UnitCostEntry(it.unitCostId!!, it.pricePerUnit, it.unitType) }
        )
        budgetCostValidator.validateAllowedSpfCosts(projectPersistence.getProjectCallSettings(projectId))

        budgetCostValidator.validateBudgetPeriods(
            spfCosts.map { it.budgetPeriods }.flatten().toSet(),
            projectPersistence.getProjectPeriods(projectId).map { it.number }.toSet()
        )

        persistence.deleteAllBudgetSpfCostsExceptFor(
            partnerId = partnerId,
            idsToKeep = spfCosts.mapNotNullTo(HashSet()) { it.id }
        )

        return persistence.createOrUpdateBudgetSpfCosts(
            projectId,
            partnerId,
            spfCosts.map {
                it.apply {
                    it.rowSum = calculateRowSum(it)
                    this.truncateBaseEntryNumbers()
                    this.pricePerUnit.truncate()
                }
            }.toList()
        )
    }

    private fun calculateRowSum(spfCostEntry: BudgetSpfCostEntry) =
        spfCostEntry.pricePerUnit.multiply(spfCostEntry.numberOfUnits).truncate()

}
