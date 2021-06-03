package io.cloudflight.jems.server.project.entity.partner.cofinancing

import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerCoFinancingFundType
import io.cloudflight.jems.server.common.entity.TranslationView
import java.math.BigDecimal

interface PartnerFinancingRow: TranslationView {
    val type: ProjectPartnerCoFinancingFundType
    val percentage: BigDecimal
    val fundId: Long?
    val selected: Boolean?
    val fundType: String?
    val abbreviation: String?
    val description: String?
}