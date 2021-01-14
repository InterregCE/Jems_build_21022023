package io.cloudflight.jems.api.project.dto.partner.budget

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.swagger.annotations.ApiModel
import java.math.BigDecimal

@ApiModel(value = "BudgetTravelAndAccommodationCostEntryDTO", parent = BaseBudgetEntryDTO::class)
data class BudgetTravelAndAccommodationCostEntryDTO(
    override val id: Long? = null,
    override val numberOfUnits: BigDecimal,
    override val pricePerUnit: BigDecimal,
    override val rowSum: BigDecimal,
    val unitType: Set<InputTranslation> = emptySet(),
    val description: Set<InputTranslation> = emptySet()
) : BaseBudgetEntryDTO