package io.cloudflight.jems.server.payments.model.advance

import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRoleDTO
import io.cloudflight.jems.server.call.service.model.IdNamePair
import io.cloudflight.jems.server.programme.service.fund.model.ProgrammeFund
import java.math.BigDecimal
import java.time.LocalDate

data class AdvancePayment (
    val id: Long,
    val projectCustomIdentifier: String,
    val projectAcronym: String,

    val partnerType: ProjectPartnerRoleDTO,
    val partnerNumber: Int?,
    val partnerAbbreviation: String,

    val programmeFund: ProgrammeFund? = null,
    val partnerContribution: IdNamePair? = null,
    val partnerContributionSpf: IdNamePair? = null,

    val paymentAuthorized: Boolean? = null,
    val amountPaid: BigDecimal?,
    val paymentDate: LocalDate? = null,
    val amountSettled: BigDecimal?
)
