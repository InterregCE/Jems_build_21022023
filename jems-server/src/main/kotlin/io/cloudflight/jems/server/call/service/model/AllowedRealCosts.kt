package io.cloudflight.jems.server.call.service.model

data class AllowedRealCosts(
    val allowRealStaffCosts: Boolean,
    val allowRealTravelAndAccommodationCosts: Boolean,
    var allowRealExternalExpertiseAndServicesCosts: Boolean,
    var allowRealEquipmentCosts: Boolean,
    var allowRealInfrastructureCosts: Boolean,
)
