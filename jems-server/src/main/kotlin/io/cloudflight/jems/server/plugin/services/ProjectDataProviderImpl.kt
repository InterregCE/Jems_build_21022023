package io.cloudflight.jems.server.plugin.services

import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.PartnerBudgetData
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.server.programme.service.costoption.ProgrammeLumpSumPersistence
import io.cloudflight.jems.server.programme.service.legalstatus.ProgrammeLegalStatusPersistence
import io.cloudflight.jems.server.project.service.ProjectDescriptionPersistence
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.associatedorganization.AssociatedOrganizationPersistence
import io.cloudflight.jems.server.project.service.budget.model.BudgetCostsCalculationResult
import io.cloudflight.jems.server.project.service.cofinancing.get_project_cofinancing_overview.CoFinancingOverviewCalculator
import io.cloudflight.jems.server.project.service.cofinancing.model.ProjectCoFinancingOverview
import io.cloudflight.jems.server.project.service.common.BudgetCostsCalculatorService
import io.cloudflight.jems.server.project.service.lumpsum.ProjectLumpSumPersistence
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetCostsPersistence
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetOptionsPersistence
import io.cloudflight.jems.server.project.service.partner.cofinancing.ProjectPartnerCoFinancingPersistence
import io.cloudflight.jems.server.project.service.partner.model.BudgetCosts
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerBudgetOptions
import io.cloudflight.jems.server.project.service.result.ProjectResultPersistence
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal.ZERO

@Service
class ProjectDataProviderImpl(
    private val projectPersistence: ProjectPersistence,
    private val projectVersionPersistence: ProjectVersionPersistence,
    private val programmeLumpSumPersistence: ProgrammeLumpSumPersistence,
    private val projectDescriptionPersistence: ProjectDescriptionPersistence,
    private val workPackagePersistence: WorkPackagePersistence,
    private val resultPersistence: ProjectResultPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val associatedOrganizationPersistence: AssociatedOrganizationPersistence,
    private val budgetOptionsPersistence: ProjectPartnerBudgetOptionsPersistence,
    private val coFinancingPersistence: ProjectPartnerCoFinancingPersistence,
    private val getBudgetCostsPersistence: ProjectPartnerBudgetCostsPersistence,
    private val budgetCostsCalculator: BudgetCostsCalculatorService,
    private val projectLumpSumPersistence: ProjectLumpSumPersistence,
    private val programmeLegalStatusPersistence: ProgrammeLegalStatusPersistence,
) : ProjectDataProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(ProjectDataProviderImpl::class.java)
    }

    @Transactional(readOnly = true)
    override fun getProjectDataForProjectId(projectId: Long, version: String?): ProjectData {
        val project = projectPersistence.getProject(projectId, version)
        val legalStatuses = programmeLegalStatusPersistence.getMax20Statuses()

        val partners = partnerPersistence.findTop30ByProjectId(projectId, version).map { partner ->
            val budgetOptions = budgetOptionsPersistence.getBudgetOptions(partner.id, version)
            val coFinancing = coFinancingPersistence.getCoFinancingAndContributions(partner.id, version).toDataModel()
            val budgetCosts = BudgetCosts(
                staffCosts = getBudgetCostsPersistence.getBudgetStaffCosts(partner.id, version),
                travelCosts = getBudgetCostsPersistence.getBudgetTravelAndAccommodationCosts(partner.id, version),
                externalCosts = getBudgetCostsPersistence.getBudgetExternalExpertiseAndServicesCosts(
                    partner.id,
                    version
                ),
                equipmentCosts = getBudgetCostsPersistence.getBudgetEquipmentCosts(partner.id, version),
                infrastructureCosts = getBudgetCostsPersistence.getBudgetInfrastructureAndWorksCosts(
                    partner.id,
                    version
                ),
                unitCosts = getBudgetCostsPersistence.getBudgetUnitCosts(partner.id, version),
            ).toDataModel()
            val budgetCalculationResult = getBudgetTotalCosts(budgetOptions, partner.id, version).toDataModel()
            val budget = PartnerBudgetData(
                budgetOptions?.toDataModel(),
                coFinancing,
                budgetCosts,
                budgetCalculationResult.totalCosts,
                budgetCalculationResult
            )
            val stateAid = partnerPersistence.getPartnerStateAid(partnerId = partner.id, version)

            partner.toDataModel(
                stateAid,
                budget,
                legalStatuses.firstOrNull { it.id == partner.legalStatusId }?.description ?: emptySet()
            )
        }.toSet()

        val sectionA = project.toDataModel(tableA3data = getCoFinancingOverview(partners, version))

        val sectionB =
            ProjectDataSectionB(
                partners,
                associatedOrganizationPersistence.findAllByProjectId(projectId, version).toDataModel()
            )

        val sectionC = projectDescriptionPersistence.getProjectDescription(projectId, version).toDataModel(
            workPackages = workPackagePersistence.getWorkPackagesWithAllDataByProjectId(projectId, version),
            results = resultPersistence.getResultsForProject(projectId, version)
        )

        val sectionE = with(projectLumpSumPersistence.getLumpSums(projectId, version)) {
            this.toDataModel(programmeLumpSumPersistence.getLumpSums(this.map { it.programmeLumpSumId }))
        }

        logger.info("Retrieved project data for project id=$projectId via plugin.")

        return ProjectData(
            sectionA, sectionB, sectionC, sectionE,
            lifecycleData = ProjectLifecycleData(status = project.projectStatus.status.toDataModel()),
            versions = projectVersionPersistence.getAllVersionsByProjectId(projectId).toDataModel()
        )
    }

    private fun getBudgetTotalCosts(
        budgetOptions: ProjectPartnerBudgetOptions?,
        partnerId: Long,
        version: String?
    ): BudgetCostsCalculationResult {
        val unitCostTotal = getBudgetCostsPersistence.getBudgetUnitCostTotal(partnerId, version)
        val lumpSumsTotal = getBudgetCostsPersistence.getBudgetLumpSumsCostTotal(partnerId, version)
        val equipmentCostTotal = getBudgetCostsPersistence.getBudgetEquipmentCostTotal(partnerId, version)
        val externalCostTotal =
            getBudgetCostsPersistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId, version)
        val infrastructureCostTotal =
            getBudgetCostsPersistence.getBudgetInfrastructureAndWorksCostTotal(partnerId, version)

        val travelCostTotal = if (budgetOptions?.travelAndAccommodationOnStaffCostsFlatRate == null)
            getBudgetCostsPersistence.getBudgetTravelAndAccommodationCostTotal(partnerId, version)
        else
            ZERO

        val staffCostTotal = if (budgetOptions?.staffCostsFlatRate == null)
            getBudgetCostsPersistence.getBudgetStaffCostTotal(partnerId, version)
        else
            ZERO

        return budgetCostsCalculator.calculateCosts(
            budgetOptions,
            unitCostTotal,
            lumpSumsTotal,
            externalCostTotal,
            equipmentCostTotal,
            infrastructureCostTotal,
            travelCostTotal,
            staffCostTotal
        )
    }

    private fun getCoFinancingOverview(partners: Set<ProjectPartnerData>, version: String?): ProjectCoFinancingOverview {
        val partnersByIds = partners.associateBy { it.id!! }
        val funds = if (partnersByIds.keys.isNotEmpty()) coFinancingPersistence.getAvailableFunds(partnersByIds.keys.first()) else emptySet()

        return CoFinancingOverviewCalculator.calculateCoFinancingOverview(
            partnerIds = partnersByIds.keys,
            getBudgetTotalCost = { partnerId -> partnersByIds[partnerId]?.budget?.projectBudgetCostsCalculationResult?.totalCosts ?: ZERO },
            getCoFinancingAndContributions = { coFinancingPersistence.getCoFinancingAndContributions(it, version) },
            funds = funds,
        )
    }

}
