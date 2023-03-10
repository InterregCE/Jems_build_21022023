package io.cloudflight.jems.api.programme.dto.stateaid

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ProgrammeStateAidDTO (
    val id: Long,
    val measure: ProgrammeStateAidMeasure,
    val name: Set<InputTranslation> = emptySet(),
    val abbreviatedName: Set<InputTranslation> = emptySet(),
    val schemeNumber: String?,
    val maxIntensity: BigDecimal? = BigDecimal.ZERO,
    val threshold: BigDecimal? = BigDecimal.ZERO,
    val comments: Set<InputTranslation> = emptySet()
)
