package io.cloudflight.jems.server.payments.service

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.audit.service.AuditBuilder
import io.cloudflight.jems.server.common.entity.extractTranslation
import io.cloudflight.jems.server.payments.model.advance.AdvancePaymentDetail
import io.cloudflight.jems.server.payments.model.regular.PartnerPayment
import io.cloudflight.jems.server.payments.model.regular.PaymentDetail
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole

fun monitoringFtlsReadyForPayment(
    context: Any,
    project: ProjectSummary,
    ftlsId: Int,
    state: Boolean
): AuditCandidateEvent =
    AuditCandidateEvent(
        context = context,
        auditCandidate = AuditBuilder(AuditAction.FTLS_READY_FOR_PAYMENT_CHANGE)
            .project(project)
            .description("Fast track lump sum $ftlsId for project ${project.customIdentifier}" +
                " set as ${getAnswer(state)} for Ready for payment")
            .build()
    )

fun paymentInstallmentCreated(
    context: Any,
    payment: PaymentDetail,
    partner: PartnerPayment,
    installmentNr: Int,
): AuditCandidateEvent =
    AuditCandidateEvent(
        context = context,
        auditCandidate = AuditBuilder(AuditAction.PAYMENT_INSTALLMENT_AUTHORISED)
            .project(partner.projectId, payment.projectCustomIdentifier, payment.projectAcronym)
            .description("Payment details for payment ${payment.id}, installment $installmentNr" +
                " of partner ${getPartnerName(partner.partnerRole, partner.partnerNumber)} are authorised")
            .build()
    )

fun paymentInstallmentConfirmed(
    context: Any,
    payment: PaymentDetail,
    partner: PartnerPayment,
    installmentNr: Int,
): AuditCandidateEvent =
    AuditCandidateEvent(
        context = context,
        auditCandidate = AuditBuilder(AuditAction.PAYMENT_INSTALLMENT_CONFIRMED)
            .project(partner.projectId, payment.projectCustomIdentifier, payment.projectAcronym)
            .description("Payment details for payment ${payment.id}, installment $installmentNr" +
                " of partner ${getPartnerName(partner.partnerRole, partner.partnerNumber)} are confirmed")
            .build()
    )

fun paymentInstallmentDeleted(
    context: Any,
    payment: PaymentDetail,
    partner: PartnerPayment,
    installmentNr: Int,
): AuditCandidateEvent =
    AuditCandidateEvent(
        context = context,
        auditCandidate = AuditBuilder(AuditAction.PAYMENT_INSTALLMENT_IS_DELETED)
            .project(partner.projectId, payment.projectCustomIdentifier, payment.projectAcronym)
            .description("Payment installment $installmentNr for payment ${payment.id}" +
                " of partner ${getPartnerName(partner.partnerRole, partner.partnerNumber)} is deleted")
            .build()
    )


fun advancePaymentCreated(
    context: Any,
    paymentDetail: AdvancePaymentDetail
): AuditCandidateEvent = AuditCandidateEvent(
    context = context,
    auditCandidate = AuditBuilder(AuditAction.ADVANCE_PAYMENT_IS_CREATED)
        .project(paymentDetail.projectId, paymentDetail.projectCustomIdentifier, paymentDetail.projectAcronym)
        .description("Advance payment number ${paymentDetail.id} is created for " +
            "partner ${getPartnerName(paymentDetail.partnerType, paymentDetail.partnerNumber)} for funding source " +
            getFundingSourceName(paymentDetail))
        .build()
)
fun advancePaymentDeleted(
    context: Any,
    paymentDetail: AdvancePaymentDetail
): AuditCandidateEvent = AuditCandidateEvent(
    context = context,
    auditCandidate = AuditBuilder(AuditAction.ADVANCE_PAYMENT_IS_DELETED)
        .project(paymentDetail.projectId, paymentDetail.projectCustomIdentifier, paymentDetail.projectAcronym)
        .description("Advance payment number ${paymentDetail.id} is deleted for " +
            "partner ${getPartnerName(paymentDetail.partnerType, paymentDetail.partnerNumber)} for funding source " +
            getFundingSourceName(paymentDetail))
        .build()
)

fun advancePaymentAuthorized(
    context: Any,
    paymentDetail: AdvancePaymentDetail
): AuditCandidateEvent = AuditCandidateEvent(
    context = context,
    auditCandidate = AuditBuilder(AuditAction.ADVANCE_PAYMENT_DETAIL_AUTHORISED)
        .project(paymentDetail.projectId, paymentDetail.projectCustomIdentifier, paymentDetail.projectAcronym)
        .description("Advance payment details for advance payment ${paymentDetail.id} of " +
            "partner ${getPartnerName(paymentDetail.partnerType, paymentDetail.partnerNumber)} " +
            "for funding source ${getFundingSourceName(paymentDetail)} are authorised")
        .build()
)

fun advancePaymentConfirmed(
    context: Any,
    paymentDetail: AdvancePaymentDetail
): AuditCandidateEvent = AuditCandidateEvent(
    context = context,
    auditCandidate = AuditBuilder(AuditAction.ADVANCE_PAYMENT_DETAIL_CONFIRMED)
        .project(paymentDetail.projectId, paymentDetail.projectCustomIdentifier, paymentDetail.projectAcronym)
        .description("Advance payment details for advance payment ${paymentDetail.id} of " +
            "partner ${getPartnerName(paymentDetail.partnerType, paymentDetail.partnerNumber)} " +
            "for funding source ${getFundingSourceName(paymentDetail)} are confirmed")
        .build()
)

private fun getAnswer(state: Boolean): String {
    return if (state) { "YES" } else { "NO" }
}

private fun getFundingSourceName(paymentDetail: AdvancePaymentDetail): String {
    return when {
        paymentDetail.programmeFund != null ->
            "(${paymentDetail.programmeFund.id}, ${paymentDetail.programmeFund.type})"
        paymentDetail.partnerContribution != null -> paymentDetail.partnerContribution.name
        paymentDetail.partnerContributionSpf != null -> paymentDetail.partnerContributionSpf.name
        else -> ""
    }
}

private fun getPartnerName(partnerRole: ProjectPartnerRole, partnerNumber: Int?): String =
    partnerRole.isLead.let {
        if (it) "LP${partnerNumber}" else "PP${partnerNumber}"
    }
