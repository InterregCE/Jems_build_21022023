package io.cloudflight.jems.server.call.service.update_call_flat_rates

import io.cloudflight.jems.api.call.dto.CallStatus
import io.cloudflight.jems.api.call.dto.flatrate.FlatRateType
import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.call.service.CallPersistence
import io.cloudflight.jems.server.call.service.model.CallDetail
import io.cloudflight.jems.server.call.service.model.ProjectCallFlatRate
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

class UpdateCallFlatRatesTest: UnitTest() {

    companion object {
        private fun callWithStatus(id: Long, status: CallStatus) = CallDetail(
            id = id,
            name = "",
            status = status,
            startDate = ZonedDateTime.now().minusDays(1),
            endDate = ZonedDateTime.now().plusDays(1),
            isAdditionalFundAllowed = true,
            lengthOfPeriod = 7,
        )

    }

    @MockK
    lateinit var persistence: CallPersistence

    @RelaxedMockK
    lateinit var auditService: AuditService

    @InjectMockKs
    private lateinit var updateCallFlatRates: UpdateCallFlatRates

    @Test
    fun `updateFlatRateSetup - change of flat rates when call is PUBLISHED`() {
        val ID = 9L
        val existing = ProjectCallFlatRate(
            type = FlatRateType.OTHER_COSTS_ON_STAFF_COSTS,
            rate = 10,
            isAdjustable = false
        )
        val toBeCreated = ProjectCallFlatRate(
            type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS,
            rate = 5,
            isAdjustable = true
        )

        val call = callWithStatus(id = ID, CallStatus.PUBLISHED).copy(flatRates = sortedSetOf(existing))
        every { persistence.getCallById(ID) } returns call

        val slotFlatRate = slot<Set<ProjectCallFlatRate>>()
        every { persistence.updateProjectCallFlatRate(ID, capture(slotFlatRate)) } returns call
        updateCallFlatRates.updateFlatRateSetup(ID, setOf(existing, toBeCreated))

        verify(exactly = 1) { persistence.updateProjectCallFlatRate(ID, any()) }
        assertThat(slotFlatRate.captured).containsExactlyInAnyOrder(existing, toBeCreated)
    }

    @Test
    fun `updateFlatRateSetup - change of flat rates when call is DRAFT`() {
        val ID = 10L
        val existingToBeRemoved = ProjectCallFlatRate(
            type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS,
            rate = 7,
            isAdjustable = false
        )
        val existingToBeUpdated = ProjectCallFlatRate(
            type = FlatRateType.OTHER_COSTS_ON_STAFF_COSTS,
            rate = 7,
            isAdjustable = false
        )
        val newToBeUpdated = ProjectCallFlatRate(
            type = FlatRateType.OTHER_COSTS_ON_STAFF_COSTS,
            rate = 10,
            isAdjustable = true
        )

        val call = callWithStatus(id = ID, CallStatus.DRAFT).copy(flatRates = sortedSetOf(existingToBeRemoved, existingToBeUpdated))
        every { persistence.getCallById(ID) } returns call


        val slotFlatRate = slot<Set<ProjectCallFlatRate>>()
        every { persistence.updateProjectCallFlatRate(ID, capture(slotFlatRate)) } returns call
        updateCallFlatRates.updateFlatRateSetup(ID, setOf(newToBeUpdated))

        verify(exactly = 1) { persistence.updateProjectCallFlatRate(ID, any()) }
        assertThat(slotFlatRate.captured).containsExactlyInAnyOrder(newToBeUpdated)
    }

    @Test
    fun `updateFlatRateSetup - forbidden to remove existing flat rate when call is published`() {
        val ID = 11L
        val existing = ProjectCallFlatRate(
            type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS,
            rate = 7,
            isAdjustable = false
        )
        val call = callWithStatus(id = ID, CallStatus.PUBLISHED).copy(flatRates = sortedSetOf(existing))
        every { persistence.getCallById(ID) } returns call

        every { persistence.updateProjectCallFlatRate(ID, any()) } returns call

        val ex = assertThrows<FlatRatesRemovedAfterCallPublished> { updateCallFlatRates.updateFlatRateSetup(ID, emptySet()) }
        assertThat(ex.message).isEqualTo("Following flat rates cannot be changed: [${FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS}]")
    }

    @Test
    fun `updateFlatRateSetup - forbidden to change existing flat rate when call is published`() {
        val ID = 12L
        val existing = ProjectCallFlatRate(
            type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS,
            rate = 7,
            isAdjustable = false
        )
        val call = callWithStatus(id = ID, CallStatus.PUBLISHED).copy(flatRates = sortedSetOf(existing))
        every { persistence.getCallById(ID) } returns call
        every { persistence.updateProjectCallFlatRate(ID, any()) } returns call

        val toUpdate = existing.copy(rate = 8)
        val ex = assertThrows<FlatRatesRemovedAfterCallPublished> { updateCallFlatRates.updateFlatRateSetup(ID, setOf(toUpdate)) }
        assertThat(ex.message).isEqualTo("Following flat rates cannot be changed: [${FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS}]")
    }

    @Test
    fun `updateFlatRateSetup invalid - duplicates`() {
        val toBeSet = setOf(
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS,
                rate = 5,
                isAdjustable = true
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS,
                rate = 9,
                isAdjustable = false
            )
        )
        assertThrows<DuplicateFlatRateTypesDefined> { updateCallFlatRates.updateFlatRateSetup(1, toBeSet) }
    }

    @Test
    fun `updateFlatRateSetup invalid - over max flat rate`() {
        val toBeSet = setOf(
            ProjectCallFlatRate(
                type = FlatRateType.STAFF_COSTS,
                rate = 21,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS,
                rate = 16,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS,
                rate = 26,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.TRAVEL_AND_ACCOMMODATION_ON_STAFF_COSTS,
                rate = 16,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OTHER_COSTS_ON_STAFF_COSTS,
                rate = 41,
                isAdjustable = true,
            )
        )
        val ex = assertThrows<FlatRateOutOfBounds> { updateCallFlatRates.updateFlatRateSetup(1, toBeSet) }
        assertThat(ex.formErrors).isEqualTo(mapOf(
            FlatRateType.STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.TRAVEL_AND_ACCOMMODATION_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.OTHER_COSTS_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range")
        ))
    }

    @Test
    fun `updateFlatRateSetup invalid - below min flat rate`() {
        val toBeSet = setOf(
            ProjectCallFlatRate(
                type = FlatRateType.STAFF_COSTS,
                rate = 0,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS,
                rate = 0,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS,
                rate = 0,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.TRAVEL_AND_ACCOMMODATION_ON_STAFF_COSTS,
                rate = 0,
                isAdjustable = true,
            ),
            ProjectCallFlatRate(
                type = FlatRateType.OTHER_COSTS_ON_STAFF_COSTS,
                rate = 0,
                isAdjustable = true,
            )
        )
        val ex = assertThrows<FlatRateOutOfBounds> { updateCallFlatRates.updateFlatRateSetup(1, toBeSet) }
        assertThat(ex.formErrors).isEqualTo(mapOf(
            FlatRateType.OTHER_COSTS_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.TRAVEL_AND_ACCOMMODATION_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.OFFICE_AND_ADMINISTRATION_ON_OTHER_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.OFFICE_AND_ADMINISTRATION_ON_STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range"),
            FlatRateType.STAFF_COSTS.name to I18nMessage(i18nKey = "call.flatRateSetup.rate.out.of.range")
        ))
    }

}