package io.cloudflight.jems.api.programme.dto.indicator

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ResultIndicatorSummaryDTO(
    val id: Long?,
    val identifier: String,
    val code: String?,
    val name: Set<InputTranslation> = emptySet(),
    val programmePriorityCode: String?,
    val measurementUnit: Set<InputTranslation> = emptySet(),
    val baseline: BigDecimal?
)
