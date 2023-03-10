package io.cloudflight.jems.server.project.controller.lumpsum

import io.cloudflight.jems.api.project.dto.lumpsum.ProjectLumpSumDTO
import io.cloudflight.jems.api.project.dto.lumpsum.ProjectPartnerLumpSumDTO
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectLumpSum
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectPartnerLumpSum
import java.math.BigDecimal

fun List<ProjectLumpSum>.toDto() = map {
    ProjectLumpSumDTO(
        orderNr = it.orderNr,
        programmeLumpSumId = it.programmeLumpSumId,
        period = it.period,
        lumpSumContributions = it.lumpSumContributions.map { it.toDto() },
        readyForPayment = it.readyForPayment,
        comment = it.comment,
        fastTrack = it.fastTrack,
        paymentEnabledDate = it.paymentEnabledDate,
        lastApprovedVersionBeforeReadyForPayment = it.lastApprovedVersionBeforeReadyForPayment
    )
}

fun ProjectPartnerLumpSum.toDto() = ProjectPartnerLumpSumDTO(
    partnerId = partnerId,
    amount = amount,
)

fun List<ProjectLumpSumDTO>.toModel() = map {
    ProjectLumpSum(
        orderNr = it.orderNr,
        programmeLumpSumId = it.programmeLumpSumId,
        period = it.period,
        lumpSumContributions = it.lumpSumContributions.toPartnerContributionModel(),
        readyForPayment = it.readyForPayment,
        comment = it.comment,
        fastTrack = it.fastTrack,
        paymentEnabledDate = it.paymentEnabledDate,
        lastApprovedVersionBeforeReadyForPayment = it.lastApprovedVersionBeforeReadyForPayment
    )
}

fun ProjectPartnerLumpSumDTO.toPartnerContributionModel() =
    ProjectPartnerLumpSum(
        partnerId = partnerId,
        amount = amount,
    )

fun List<ProjectPartnerLumpSumDTO>.toPartnerContributionModel() =
    filter { it.amount.compareTo(BigDecimal.ZERO) != -1 }
        .map { it.toPartnerContributionModel() }
