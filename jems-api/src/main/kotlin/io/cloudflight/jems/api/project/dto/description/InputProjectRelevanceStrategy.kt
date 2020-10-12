package io.cloudflight.jems.api.project.dto.description

import io.cloudflight.jems.api.strategy.ProgrammeStrategy

data class InputProjectRelevanceStrategy(
    val strategy: ProgrammeStrategy? = null,
    val specification: String? = null
)