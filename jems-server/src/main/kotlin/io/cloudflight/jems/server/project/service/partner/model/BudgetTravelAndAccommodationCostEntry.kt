package io.cloudflight.jems.server.project.service.partner.model

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class BudgetTravelAndAccommodationCostEntry(
    override val id: Long? = null,
    override val numberOfUnits: BigDecimal,
    override var rowSum: BigDecimal?,
    override val budgetPeriods: MutableSet<BudgetPeriod>,
    override val unitCostId: Long? = null,
    var pricePerUnit: BigDecimal,
    var unitType: Set<InputTranslation> = emptySet(),
    val description: Set<InputTranslation> = emptySet(),
    val comments: Set<InputTranslation> = emptySet()
) : BaseBudgetEntry
