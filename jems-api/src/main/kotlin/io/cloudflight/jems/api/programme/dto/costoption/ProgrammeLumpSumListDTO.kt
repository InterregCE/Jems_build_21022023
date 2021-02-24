package io.cloudflight.jems.api.programme.dto.costoption

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class ProgrammeLumpSumListDTO(
    val id: Long? = null,
    val name: Set<InputTranslation> = emptySet(),
    val cost: BigDecimal? = null
)
