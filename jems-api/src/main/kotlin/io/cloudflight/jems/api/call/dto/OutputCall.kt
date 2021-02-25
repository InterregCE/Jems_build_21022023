package io.cloudflight.jems.api.call.dto

import io.cloudflight.jems.api.call.dto.flatrate.FlatRateSetupDTO
import io.cloudflight.jems.api.programme.dto.fund.ProgrammeFundDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeUnitCostDTO
import io.cloudflight.jems.api.programme.dto.priority.OutputProgrammePriorityPolicySimple
import io.cloudflight.jems.api.programme.dto.strategy.ProgrammeStrategy
import io.cloudflight.jems.api.project.dto.InputTranslation
import java.time.ZonedDateTime

data class OutputCall (
    val id: Long?,
    val name: String,
    val priorityPolicies: List<OutputProgrammePriorityPolicySimple>,
    val strategies: List<ProgrammeStrategy>,
    val funds: List<ProgrammeFundDTO>,
    val isAdditionalFundAllowed: Boolean,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val status: CallStatus,
    val lengthOfPeriod: Int?,
    val description: Set<InputTranslation> = emptySet(),
    val flatRates: FlatRateSetupDTO,
    val lumpSums: List<ProgrammeLumpSumDTO> = emptyList(),
    val unitCosts: List<ProgrammeUnitCostDTO> = emptyList()
)
