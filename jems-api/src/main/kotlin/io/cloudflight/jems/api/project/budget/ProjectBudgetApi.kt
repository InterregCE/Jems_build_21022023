package io.cloudflight.jems.api.project.budget

import io.cloudflight.jems.api.project.dto.budget.ProjectBudgetOverviewPerPartnerPerPeriodDTO
import io.cloudflight.jems.api.project.dto.budget.ProjectPartnerBudgetPerFundDTO
import io.cloudflight.jems.api.project.dto.budget.ProjectUnitCostDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Api("Project Budget")
interface ProjectBudgetApi {

    companion object {
        private const val ENDPOINT_API_PROJECT_BUDGET = "/api/project/{projectId}/budget"
    }

    @ApiOperation("Get project partner budget per period")
    @GetMapping("$ENDPOINT_API_PROJECT_BUDGET/perPeriod")
    fun getProjectPartnerBudgetPerPeriod(
        @PathVariable projectId: Long,
        @RequestParam(required = false) version: String? = null
    ): ProjectBudgetOverviewPerPartnerPerPeriodDTO

    @ApiOperation("Get project unit costs")
    @GetMapping("$ENDPOINT_API_PROJECT_BUDGET/unitCosts")
    fun getProjectUnitCosts(
        @PathVariable projectId: Long,
        @RequestParam(required = false) version: String? = null
    ): List<ProjectUnitCostDTO>

    @ApiOperation("Get project partner budget per fund")
    @GetMapping("$ENDPOINT_API_PROJECT_BUDGET/perFund")
    fun getProjectPartnerBudgetPerFund(
        @PathVariable projectId: Long,
        @RequestParam(required = false) version: String? = null
    ): List<ProjectPartnerBudgetPerFundDTO>
}
