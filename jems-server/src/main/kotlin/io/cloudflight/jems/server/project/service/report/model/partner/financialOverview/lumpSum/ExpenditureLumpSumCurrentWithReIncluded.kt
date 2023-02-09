package io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.lumpSum

import java.math.BigDecimal

data class ExpenditureLumpSumCurrentWithReIncluded(
    val current: BigDecimal,
    val currentReIncluded: BigDecimal,
)
