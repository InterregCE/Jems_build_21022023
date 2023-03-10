package io.cloudflight.jems.api.project.dto.report.partner.identification.control

data class ReportVerificationDTO(
    val generalMethodologies: Set<ReportMethodologyDTO>,
    val verificationInstances: Set<ReportOnTheSpotVerificationDTO>,
    val riskBasedVerificationApplied: Boolean,
    val riskBasedVerificationDescription: String?
)
