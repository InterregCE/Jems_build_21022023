package io.cloudflight.jems.server.project.repository.report.expenditure

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.common.entity.addTranslationEntities
import io.cloudflight.jems.server.common.entity.extractTranslation
import io.cloudflight.jems.server.project.entity.report.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportExpenditureCostEntity
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportExpenditureCostTranslEntity
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportInvestmentEntity
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportLumpSumEntity
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportUnitCostEntity
import io.cloudflight.jems.server.project.repository.report.toModel
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerControlReportExpenditureVerification
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerControlReportExpenditureVerificationUpdate
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportExpenditureCost
import java.math.BigDecimal

fun List<PartnerReportExpenditureCostEntity>.toModel() = map {
    ProjectPartnerReportExpenditureCost(
        id = it.id,
        lumpSumId = it.reportLumpSum?.id,
        unitCostId = it.reportUnitCost?.id,
        costCategory = it.costCategory,
        investmentId = it.reportInvestment?.id,
        contractId = it.procurementId,
        internalReferenceNumber = it.internalReferenceNumber,
        invoiceNumber = it.invoiceNumber,
        invoiceDate = it.invoiceDate,
        dateOfPayment = it.dateOfPayment,
        totalValueInvoice = it.totalValueInvoice,
        vat = it.vat,
        numberOfUnits = it.numberOfUnits,
        pricePerUnit = it.pricePerUnit,
        declaredAmount = it.declaredAmount,
        currencyCode = it.currencyCode,
        currencyConversionRate = it.currencyConversionRate,
        declaredAmountAfterSubmission = it.declaredAmountAfterSubmission,
        attachment = it.attachment?.toModel(),
        comment = it.translatedValues.mapTo(HashSet()) { InputTranslation(it.translationId.language, it.comment) },
        description = it.translatedValues.mapTo(HashSet()) { InputTranslation(it.translationId.language, it.description) }
    )
}

fun List<PartnerReportExpenditureCostEntity>.toExtendedModel() = map {
    ProjectPartnerControlReportExpenditureVerification(
        id = it.id,
        lumpSumId = it.reportLumpSum?.id,
        unitCostId = it.reportUnitCost?.id,
        costCategory = it.costCategory,
        investmentId = it.reportInvestment?.id,
        contractId = it.procurementId,
        internalReferenceNumber = it.internalReferenceNumber,
        invoiceNumber = it.invoiceNumber,
        invoiceDate = it.invoiceDate,
        dateOfPayment = it.dateOfPayment,
        totalValueInvoice = it.totalValueInvoice,
        vat = it.vat,
        numberOfUnits = it.numberOfUnits,
        pricePerUnit = it.pricePerUnit,
        declaredAmount = it.declaredAmount,
        currencyCode = it.currencyCode,
        currencyConversionRate = it.currencyConversionRate,
        declaredAmountAfterSubmission = it.declaredAmountAfterSubmission,
        attachment = it.attachment?.toModel(),
        comment = it.translatedValues.mapTo(HashSet()) { InputTranslation(it.translationId.language, it.comment) },
        description = it.translatedValues.mapTo(HashSet()) { InputTranslation(it.translationId.language, it.description) },
        partOfSample = it.partOfSample,
        certifiedAmount = it.certifiedAmount,
        deductedAmount = it.deductedAmount,
        typologyOfErrorId = it.typologyOfErrorId,
        verificationComment = it.verificationComment
    )
}

fun ProjectPartnerReportExpenditureCost.toEntity(
    reportEntity: ProjectPartnerReportEntity,
    lumpSums: Map<Long, PartnerReportLumpSumEntity>,
    unitCosts: Map<Long, PartnerReportUnitCostEntity>,
    investments: Map<Long, PartnerReportInvestmentEntity>,
) =
    PartnerReportExpenditureCostEntity(
        id = id ?: 0L,
        partnerReport = reportEntity,
        reportLumpSum = if (lumpSumId != null) lumpSums[lumpSumId] else null,
        reportUnitCost = if (unitCostId != null) unitCosts[unitCostId] else null,
        costCategory = costCategory,
        reportInvestment = if (investmentId != null) investments[investmentId] else null,
        procurementId = contractId,
        internalReferenceNumber = internalReferenceNumber,
        invoiceNumber = invoiceNumber,
        invoiceDate = invoiceDate,
        dateOfPayment = dateOfPayment,
        totalValueInvoice = totalValueInvoice,
        vat = vat,
        numberOfUnits = numberOfUnits,
        pricePerUnit = pricePerUnit,
        declaredAmount = declaredAmount,
        currencyCode = currencyCode,
        currencyConversionRate = currencyConversionRate,
        declaredAmountAfterSubmission = declaredAmountAfterSubmission,
        translatedValues = mutableSetOf(),
        attachment = null,
        partOfSample = false,
        certifiedAmount = declaredAmountAfterSubmission ?: BigDecimal.ZERO,
        deductedAmount = BigDecimal.ZERO,
        typologyOfErrorId = null,
        verificationComment = null
    ).apply {
        translatedValues.addTranslation(this, comment, description)
    }

fun MutableSet<PartnerReportExpenditureCostTranslEntity>.addTranslation(
    sourceEntity: PartnerReportExpenditureCostEntity,
    comment: Set<InputTranslation>,
    description: Set<InputTranslation>
) =
    this.addTranslationEntities(
        { language ->
            PartnerReportExpenditureCostTranslEntity(
                translationId = TranslationId(sourceEntity, language),
                comment = comment.extractTranslation(language),
                description = description.extractTranslation(language),
            )
        }, arrayOf(comment, description)
    )

fun ProjectPartnerReportExpenditureCost.toVerificationData() =
    ProjectPartnerControlReportExpenditureVerificationUpdate(
        id = id ?: 0,
        partOfSample = false,
        certifiedAmount = declaredAmountAfterSubmission ?: BigDecimal.ZERO,
        deductedAmount = BigDecimal.ZERO,
        typologyOfErrorId = null,
        verificationComment = null
    )