package io.cloudflight.jems.server.project.service.workpackage.output.model

import io.cloudflight.jems.api.project.dto.InputTranslation
import java.math.BigDecimal

data class WorkPackageOutput(
    val workPackageId: Long,
    val outputNumber: Int = 0,
    val programmeOutputIndicatorId: Long? = null,
    val programmeOutputIndicatorIdentifier: String? = null,
    val targetValue: BigDecimal? = null,
    val periodNumber: Int? = null,
    val title: Set<InputTranslation> = emptySet(),
    val description: Set<InputTranslation> =  emptySet()
)
