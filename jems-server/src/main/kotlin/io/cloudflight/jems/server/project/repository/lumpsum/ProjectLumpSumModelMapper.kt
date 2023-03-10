package io.cloudflight.jems.server.project.repository.lumpsum

import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeLumpSumEntity
import io.cloudflight.jems.server.project.entity.lumpsum.ProjectLumpSumEntity
import io.cloudflight.jems.server.project.entity.lumpsum.ProjectLumpSumId
import io.cloudflight.jems.server.project.entity.lumpsum.ProjectLumpSumRow
import io.cloudflight.jems.server.project.entity.lumpsum.ProjectPartnerLumpSumEntity
import io.cloudflight.jems.server.project.entity.lumpsum.ProjectPartnerLumpSumId
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectLumpSum
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectPartnerLumpSum
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun List<ProjectLumpSumEntity>.toModel() = sortedBy { it.id.orderNr }.map {
    ProjectLumpSum(
        orderNr = it.id.orderNr,
        period = it.endPeriod,
        programmeLumpSumId = it.programmeLumpSum.id,
        lumpSumContributions = it.lumpSumContributions.toModel(),
        fastTrack = it.programmeLumpSum.isFastTrack,
        readyForPayment = it.isReadyForPayment,
        comment = it.comment,
        lastApprovedVersionBeforeReadyForPayment = it.lastApprovedVersionBeforeReadyForPayment,
        paymentEnabledDate = it.paymentEnabledDate
    )
}

fun Iterable<ProjectPartnerLumpSumEntity>.toModel() = sortedBy { it.id.projectPartner.sortNumber }
    .map {
        ProjectPartnerLumpSum(
            partnerId = it.id.projectPartner.id,
            amount = it.amount,
        )
    }

fun ProjectLumpSum.toEntity(
    projectLumpSumId: ProjectLumpSumId,
    getProgrammeLumpSum: (Long) -> ProgrammeLumpSumEntity,
    getProjectPartner: (Long) -> ProjectPartnerEntity,
): ProjectLumpSumEntity {
    return ProjectLumpSumEntity(
        id = projectLumpSumId,
        programmeLumpSum = getProgrammeLumpSum.invoke(this.programmeLumpSumId),
        endPeriod = period,
        lumpSumContributions = lumpSumContributions.toPartnerLumpSumEntity(projectLumpSumId, getProjectPartner),
        isReadyForPayment = readyForPayment,
        comment = comment,
        lastApprovedVersionBeforeReadyForPayment = lastApprovedVersionBeforeReadyForPayment,
        paymentEnabledDate = paymentEnabledDate
    )
}

fun List<ProjectLumpSum>.toEntity(
    projectId: Long,
    getProgrammeLumpSum: (Long) -> ProgrammeLumpSumEntity,
    getProjectPartner: (Long) -> ProjectPartnerEntity,
) = map { model ->
    model.toEntity(ProjectLumpSumId(projectId, model.orderNr), getProgrammeLumpSum, getProjectPartner)
}

fun List<ProjectPartnerLumpSum>.toPartnerLumpSumEntity(
    projectLumpSumId: ProjectLumpSumId,
    getProjectPartner: (Long) -> ProjectPartnerEntity
) = mapTo(HashSet()) {
    ProjectPartnerLumpSumEntity(
        id = ProjectPartnerLumpSumId(
            projectLumpSumId = projectLumpSumId,
            projectPartner = getProjectPartner.invoke(it.partnerId),
        ),
        amount = it.amount,
    )
}

fun List<ProjectLumpSumRow>.toProjectLumpSumHistoricalData() =
    this.groupBy { it.orderNr }.map { groupedRows ->
        ProjectLumpSum(
            orderNr = groupedRows.value.first().orderNr,
            programmeLumpSumId = groupedRows.value.first().programmeLumpSumId,
            period = groupedRows.value.first().endPeriod,
            lumpSumContributions = groupedRows.value
                .filter { it.projectPartnerId != null }
                .map {
                    ProjectPartnerLumpSum(
                        partnerId = it.projectPartnerId!!,
                        amount = it.amount
                    )
                }.toList(),
            fastTrack = groupedRows.value.first().fastTrack != 0,
            readyForPayment = groupedRows.value.first().readyForPayment != 0,
            comment = groupedRows.value.first().comment,
            paymentEnabledDate = if (groupedRows.value.first().paymentEnabledDate != null) {
                ZonedDateTime.of(groupedRows.value.first().paymentEnabledDate!!.toLocalDateTime(), ZoneOffset.UTC)
            } else null,
            lastApprovedVersionBeforeReadyForPayment = groupedRows.value.first().lastApprovedVersionBeforeReadyForPayment
        )
    }
