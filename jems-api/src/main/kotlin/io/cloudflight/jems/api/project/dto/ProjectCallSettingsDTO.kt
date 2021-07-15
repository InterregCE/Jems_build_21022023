package io.cloudflight.jems.api.project.dto

import io.cloudflight.jems.api.call.dto.application_form_configuration.ApplicationFormFieldConfigurationDTO
import io.cloudflight.jems.api.call.dto.flatrate.FlatRateSetupDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeUnitCostDTO
import java.time.ZonedDateTime

data class ProjectCallSettingsDTO(
    val callId: Long,
    val callName: String,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val endDateStep1: ZonedDateTime?,
    val lengthOfPeriod: Int,
    val additionalFundAllowed: Boolean,
    val flatRates: FlatRateSetupDTO,
    val lumpSums: List<ProgrammeLumpSumDTO>,
    val unitCosts: List<ProgrammeUnitCostDTO>,
    var applicationFormFieldConfigurations: MutableSet<ApplicationFormFieldConfigurationDTO>

)
