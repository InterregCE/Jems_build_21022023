package io.cloudflight.ems.programme.service.validators

import io.cloudflight.ems.api.programme.dto.ProgrammeObjectivePolicy
import io.cloudflight.ems.api.programme.dto.ProgrammeObjectivePolicy.AdvancedTechnologies
import io.cloudflight.ems.api.programme.dto.ProgrammeObjectivePolicy.ClimateChange
import io.cloudflight.ems.api.programme.validator.PriorityPolicyUniqueCodeValidator
import io.cloudflight.ems.exception.I18nFieldError
import io.cloudflight.ems.exception.I18nValidationException
import io.cloudflight.ems.programme.service.ProgrammePriorityService
import io.cloudflight.ems.programme.service.validator.PriorityPolicyUniqueCodeValidatorImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

class PriorityPolicyUniqueCodeValidatorTest {

    @MockK
    lateinit var programmePriorityService: ProgrammePriorityService

    lateinit var priorityPolicyUniqueCodeValidator: PriorityPolicyUniqueCodeValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        priorityPolicyUniqueCodeValidator = PriorityPolicyUniqueCodeValidatorImpl(programmePriorityService)
    }

    @Test
    fun `policy exists with different priority`() {
        every { programmePriorityService.getPriorityIdForPolicyIfExists(eq(AdvancedTechnologies)) } returns 6L
        every { programmePriorityService.getPriorityIdForPolicyIfExists(eq(ClimateChange)) } returns 7L

        var exception = assertThrows<I18nValidationException> {
            priorityPolicyUniqueCodeValidator.isPolicyFreeOrBelongsToThisProgramme(AdvancedTechnologies, 45L)
        }
        assertThat(exception)
            .overridingErrorMessage("we should not be able to update existing priority with policy, that belongs already to other priority")
            .isEqualTo(expectedExceptionForPolicy(AdvancedTechnologies))

        exception = assertThrows<I18nValidationException> {
            priorityPolicyUniqueCodeValidator.isPolicyFreeOrBelongsToThisProgramme(ClimateChange, null)
        }
        assertThat(exception)
            .overridingErrorMessage("we should not be able to add new priority with policy, that belongs already to other priority")
            .isEqualTo(expectedExceptionForPolicy(ClimateChange))
    }

    private fun expectedExceptionForPolicy(policy: ProgrammeObjectivePolicy): I18nValidationException {
        return I18nValidationException(
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
            i18nFieldErrors = mapOf("programme.priorityPolicy.programmeObjectivePolicy" to I18nFieldError(
                i18nKey = "programme.priorityPolicy.programmeObjectivePolicy.already.in.use",
                i8nArguments = listOf(policy.name)
            ))
        )
    }

    @Test
    fun `policy exists with same priority`() {
        every { programmePriorityService.getPriorityIdForPolicyIfExists(eq(AdvancedTechnologies)) } returns 1L
        assertTrue(priorityPolicyUniqueCodeValidator.isPolicyFreeOrBelongsToThisProgramme(AdvancedTechnologies, 1L),
            "should be valid, if you are updating priority with policy already assigned to this priority")
    }

    @Test
    fun `policy does not exist`() {
        every { programmePriorityService.getPriorityIdForPolicyIfExists(eq(AdvancedTechnologies)) } returns null
        assertTrue(priorityPolicyUniqueCodeValidator.isPolicyFreeOrBelongsToThisProgramme(AdvancedTechnologies, 1L),
            "we should be able to update existing priority with new not-used policy")
        assertTrue(priorityPolicyUniqueCodeValidator.isPolicyFreeOrBelongsToThisProgramme(AdvancedTechnologies, null),
            "we should be able to add new priority with not-used policy")
    }

}
