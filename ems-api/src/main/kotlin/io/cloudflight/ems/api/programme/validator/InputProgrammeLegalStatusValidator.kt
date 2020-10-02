package io.cloudflight.ems.api.programme.validator

import io.cloudflight.ems.api.programme.dto.InputProgrammeLegalStatusWrapper
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Constraint(validatedBy = [ProgrammeLegalStatusValidator::class])
annotation class InputProgrammeLegalStatusValidator(
    val message: String = "programme.legal.status.when.id.not.new.and.vice.versa",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ProgrammeLegalStatusValidator(private val programmeLegalStatusValidator: ProgrammeLegalStatusInputValidator) :
    ConstraintValidator<InputProgrammeLegalStatusValidator, InputProgrammeLegalStatusWrapper> {
    override fun isValid(legalStatus: InputProgrammeLegalStatusWrapper, context: ConstraintValidatorContext): Boolean {
        return programmeLegalStatusValidator.isProgrammeLegalStatusFilledInCorrectly(legalStatus, context)
    }
}

interface ProgrammeLegalStatusInputValidator {

    fun isProgrammeLegalStatusFilledInCorrectly(legalStatus: InputProgrammeLegalStatusWrapper, context: ConstraintValidatorContext): Boolean

}
