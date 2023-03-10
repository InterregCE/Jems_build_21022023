package io.cloudflight.jems.server.programme.service.indicator.update_result_indicator

import io.cloudflight.jems.server.call.service.CallPersistence
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.programme.authorization.CanUpdateProgrammeSetup
import io.cloudflight.jems.server.programme.service.indicator.ResultIndicatorPersistence
import io.cloudflight.jems.server.programme.service.indicator.indicatorEdited
import io.cloudflight.jems.server.programme.service.indicator.model.ResultIndicator
import io.cloudflight.jems.server.programme.service.indicator.model.ResultIndicatorDetail
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class UpdateResultIndicator(
    private val persistence: ResultIndicatorPersistence,
    private val generalValidator: GeneralValidatorService,
    private val callPersistence: CallPersistence,
    private val auditPublisher: ApplicationEventPublisher,
) : UpdateResultIndicatorInteractor {

    @Transactional
    @CanUpdateProgrammeSetup
    @ExceptionWrapper(UpdateResultIndicatorException::class)
    override fun updateResultIndicator(resultIndicator: ResultIndicator): ResultIndicatorDetail {
        validateInput(resultIndicator)
        validateResultIndicatorDetail(resultIndicator)

        val oldResultIndicator = persistence.getResultIndicator(resultIndicator.id!!)
        checkUpdateConstraintsAfterFirstPublishedCall(resultIndicator, oldResultIndicator)

        val savedResultIndicator = persistence.saveResultIndicator(resultIndicator)

        auditPublisher.publishEvent(
            indicatorEdited(
                context = this,
                identifier = savedResultIndicator.identifier,
                changes = oldResultIndicator.getDiff(savedResultIndicator)
            )
        )
        return savedResultIndicator
    }

    private fun validateInput(resultIndicator: ResultIndicator) =
        generalValidator.throwIfAnyIsInvalid(
            generalValidator.notNullOrZero(resultIndicator.id, "id"),
            generalValidator.notBlank(resultIndicator.identifier, "identifier"),
            generalValidator.maxLength(resultIndicator.identifier, 10, "identifier"),
            generalValidator.maxLength(resultIndicator.code, 6, "indicatorCode"),
            generalValidator.maxLength(resultIndicator.name, 255, "indicatorName"),
            generalValidator.maxLength(resultIndicator.measurementUnit, 255, "measurementUnit"),
            generalValidator.maxLength(resultIndicator.referenceYear, 10, "referenceYear"),
            generalValidator.maxLength(resultIndicator.comment, 1000, "comment"),
            generalValidator.maxLength(resultIndicator.sourceOfData, 1000, "sourceOfData"),
            generalValidator.minDecimal(resultIndicator.baseline, BigDecimal.ZERO, "baseline"),
            generalValidator.digits(resultIndicator.baseline, 9, 2, "baseline"),
            generalValidator.minDecimal(resultIndicator.finalTarget, BigDecimal.ZERO, "finalTarget"),
            generalValidator.digits(resultIndicator.finalTarget, 9, 2, "finalTarget")
        )

    private fun validateResultIndicatorDetail(resultIndicator: ResultIndicator) {
        if (persistence.isIdentifierUsedByAnotherResultIndicator(resultIndicator.id, resultIndicator.identifier))
            throw IdentifierIsUsedException()
    }


    private fun checkUpdateConstraintsAfterFirstPublishedCall(
        resultIndicator: ResultIndicator,
        oldResultIndicatorDetail: ResultIndicatorDetail
    ) {
        if (!callPersistence.hasAnyCallPublished())
            return

        val changedLockedFields = HashSet<String>()

        if (oldResultIndicatorDetail.programmeObjectivePolicy != resultIndicator.programmeObjectivePolicy)
            changedLockedFields.add("specificObjective")

        if (oldResultIndicatorDetail.getBaselineOrDefault() > resultIndicator.getBaselineOrDefault())
            changedLockedFields.add("baseline")

        if (oldResultIndicatorDetail.code != resultIndicator.code)
            changedLockedFields.add("indicatorCode")

        if (changedLockedFields.isNotEmpty())
            throw ResultIndicatorCannotBeChangedAfterCallIsPublished(changedLockedFields)
    }
}
