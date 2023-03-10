package io.cloudflight.jems.server.project.repository.report.partner.expenditure

import io.cloudflight.jems.server.common.entity.extractField
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportInvestmentEntity
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportInvestment

fun List<PartnerReportInvestmentEntity>.toModel() = map {
    ProjectPartnerReportInvestment(
        id = it.id,
        investmentId = it.investmentId,
        workPackageNumber = it.workPackageNumber,
        investmentNumber = it.investmentNumber,
        title = it.translatedValues.extractField { it.title },
        deactivated = it.deactivated,
        total = it.total,
    )
}
