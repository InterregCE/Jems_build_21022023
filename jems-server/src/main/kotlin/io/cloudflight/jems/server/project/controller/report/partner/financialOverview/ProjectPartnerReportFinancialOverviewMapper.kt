package io.cloudflight.jems.server.project.controller.report.partner.financialOverview

import io.cloudflight.jems.api.project.dto.report.partner.financialOverview.ExpenditureCoFinancingBreakdownDTO
import io.cloudflight.jems.api.project.dto.report.partner.financialOverview.ExpenditureCostCategoryBreakdownDTO
import io.cloudflight.jems.api.project.dto.report.partner.financialOverview.ExpenditureLumpSumBreakdownDTO
import io.cloudflight.jems.api.project.dto.report.partner.financialOverview.ExpenditureInvestmentBreakdownDTO
import io.cloudflight.jems.api.project.dto.report.partner.financialOverview.ExpenditureUnitCostBreakdownDTO
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.costCategory.ExpenditureCostCategoryBreakdown
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.coFinancing.ExpenditureCoFinancingBreakdown
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.lumpSum.ExpenditureLumpSumBreakdown
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.investments.ExpenditureInvestmentBreakdown
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.unitCost.ExpenditureUnitCostBreakdown
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

private val mapper = Mappers.getMapper(ProjectPartnerReportFinancialOverviewMapper::class.java)

fun ExpenditureCoFinancingBreakdown.toDto() = mapper.map(this)
fun ExpenditureCostCategoryBreakdown.toDto() = mapper.map(this)
fun ExpenditureInvestmentBreakdown.toDto() = mapper.map(this)
fun ExpenditureLumpSumBreakdown.toDto() = mapper.map(this)
fun ExpenditureUnitCostBreakdown.toDto() = mapper.map(this)

@Mapper
interface ProjectPartnerReportFinancialOverviewMapper {
    fun map(expenditureCoFinancing: ExpenditureCoFinancingBreakdown): ExpenditureCoFinancingBreakdownDTO
    fun map(expenditureCostCategory: ExpenditureCostCategoryBreakdown): ExpenditureCostCategoryBreakdownDTO
    fun map(lumpSum: ExpenditureLumpSumBreakdown): ExpenditureLumpSumBreakdownDTO
    fun map(expenditureInvestment: ExpenditureInvestmentBreakdown): ExpenditureInvestmentBreakdownDTO
    fun map(unitCost: ExpenditureUnitCostBreakdown): ExpenditureUnitCostBreakdownDTO
}
