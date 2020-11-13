package io.cloudflight.jems.server.project.controller.partner.budget

import io.cloudflight.jems.api.project.ProjectPartnerBudgetApi
import io.cloudflight.jems.api.project.dto.partner.budget.InputBudget
import io.cloudflight.jems.api.project.dto.partner.budget.ProjectPartnerBudgetOptionsDto
import io.cloudflight.jems.server.project.authorization.CanReadProjectPartner
import io.cloudflight.jems.server.project.authorization.CanUpdateProjectPartner
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetService
import io.cloudflight.jems.server.project.service.partner.budget.get_budget_options.GetBudgetOptionsInteractor
import io.cloudflight.jems.server.project.service.partner.budget.update_budget_options.UpdateBudgetOptionsInteractor
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ProjectPartnerBudgetController(
    private val projectPartnerBudgetService: ProjectPartnerBudgetService,
    private val getBudgetOptionsInteractor: GetBudgetOptionsInteractor,
    private val updateBudgetOptionsInteractor: UpdateBudgetOptionsInteractor
) : ProjectPartnerBudgetApi {

    @CanReadProjectPartner
    override fun getBudgetStaffCost(partnerId: Long): List<InputBudget> {
        return projectPartnerBudgetService.getStaffCosts(partnerId)
    }

    @CanUpdateProjectPartner
    override fun updateBudgetStaffCost(partnerId: Long, budgetCosts: List<InputBudget>): List<InputBudget> {
        return projectPartnerBudgetService.updateStaffCosts(partnerId, budgetCosts)
    }

    override fun getBudgetOptions(partnerId: Long): ProjectPartnerBudgetOptionsDto =
        getBudgetOptionsInteractor.getBudgetOptions(partnerId)?.toProjectPartnerBudgetOptionsDto()
            ?: ProjectPartnerBudgetOptionsDto(null, null)

    override fun updateBudgetOptions(partnerId: Long, budgetOptionsDto: ProjectPartnerBudgetOptionsDto) =
        updateBudgetOptionsInteractor.updateBudgetOptions(partnerId, budgetOptionsDto.officeAdministrationFlatRate, budgetOptionsDto.staffCostsFlatRate)

    @CanReadProjectPartner
    override fun getBudgetTravel(partnerId: Long): List<InputBudget> {
        return projectPartnerBudgetService.getTravel(partnerId)
    }

    @CanUpdateProjectPartner
    override fun updateBudgetTravel(partnerId: Long, travels: List<InputBudget>): List<InputBudget> {
        return projectPartnerBudgetService.updateTravel(partnerId, travels)
    }

    @CanReadProjectPartner
    override fun getBudgetExternal(partnerId: Long): List<InputBudget> {
        return projectPartnerBudgetService.getExternal(partnerId)
    }

    @CanUpdateProjectPartner
    override fun updateBudgetExternal(partnerId: Long, externals: List<InputBudget>): List<InputBudget> {
        return projectPartnerBudgetService.updateExternal(partnerId, externals)
    }

    @CanReadProjectPartner
    override fun getBudgetEquipment(partnerId: Long): List<InputBudget> {
        return projectPartnerBudgetService.getEquipment(partnerId)
    }

    @CanUpdateProjectPartner
    override fun updateBudgetEquipment(partnerId: Long, equipments: List<InputBudget>): List<InputBudget> {
        return projectPartnerBudgetService.updateEquipment(partnerId, equipments)
    }

    @CanReadProjectPartner
    override fun getBudgetInfrastructure(partnerId: Long): List<InputBudget> {
        return projectPartnerBudgetService.getInfrastructure(partnerId)
    }

    @CanUpdateProjectPartner
    override fun updateBudgetInfrastructure(partnerId: Long, infrastructures: List<InputBudget>): List<InputBudget> {
        return projectPartnerBudgetService.updateInfrastructure(partnerId, infrastructures)
    }

    @CanReadProjectPartner
    override fun getTotal(partnerId: Long): BigDecimal {
        return projectPartnerBudgetService.getTotal(partnerId)
    }

}