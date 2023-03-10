package io.cloudflight.jems.server.project.service.partner.budget.get_budget_costs

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetCostsPersistence
import io.cloudflight.jems.server.project.service.partner.model.BudgetGeneralCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetSpfCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetStaffCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetTravelAndAccommodationCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetUnitCostEntry
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class GetBudgetCostsTest : UnitTest() {

    @MockK
    lateinit var budgetCostsPersistence: ProjectPartnerBudgetCostsPersistence

    @InjectMockKs
    lateinit var getBudgetCosts: GetBudgetCosts

    @Test
    fun `should return budget costs for the specified partner`() {
        val partnerId = 1L
        val budgetStaffCostEntries = budgetStaffCostEntries()
        val budgetTravelAndAccommodationCostEntries = budgetTravelAndAccommodationCostEntries()
        val budgetGeneralCostEntries = budgetGeneralCostEntries()
        val budgetUnitCostEntries = budgetUnitCostEntries()
        val budgetSpfCostEntries = budgetSpfCostEntries()

        every { budgetCostsPersistence.getBudgetEquipmentCosts(partnerId) } returns budgetGeneralCostEntries
        every { budgetCostsPersistence.getBudgetExternalExpertiseAndServicesCosts(partnerId) } returns budgetGeneralCostEntries
        every { budgetCostsPersistence.getBudgetInfrastructureAndWorksCosts(partnerId) } returns budgetGeneralCostEntries
        every { budgetCostsPersistence.getBudgetTravelAndAccommodationCosts(partnerId) } returns budgetTravelAndAccommodationCostEntries
        every { budgetCostsPersistence.getBudgetStaffCosts(partnerId) } returns budgetStaffCostEntries
        every { budgetCostsPersistence.getBudgetUnitCosts(partnerId) } returns budgetUnitCostEntries
        every { budgetCostsPersistence.getBudgetSpfCosts(partnerId) } returns budgetSpfCostEntries

        val result = getBudgetCosts.getBudgetCosts(partnerId)

        verify(atLeast = 1) { budgetCostsPersistence.getBudgetEquipmentCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetExternalExpertiseAndServicesCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetInfrastructureAndWorksCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetTravelAndAccommodationCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetStaffCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetUnitCosts(partnerId) }
        verify(atLeast = 1) { budgetCostsPersistence.getBudgetSpfCosts(partnerId) }
        confirmVerified(budgetCostsPersistence)

        assertEquals(budgetStaffCostEntries, result.staffCosts)
        assertEquals(budgetTravelAndAccommodationCostEntries, result.travelCosts)
        assertEquals(budgetGeneralCostEntries, result.equipmentCosts)
        assertEquals(budgetGeneralCostEntries, result.externalCosts)
        assertEquals(budgetGeneralCostEntries, result.infrastructureCosts)
        assertEquals(budgetUnitCostEntries, result.unitCosts)
        assertEquals(budgetSpfCostEntries, result.spfCosts)
    }


    private fun budgetStaffCostEntries() = listOf(
        BudgetStaffCostEntry(
            1,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            null,
            BigDecimal.ONE,
            emptySet(),
            emptySet(),
            emptySet()
        ),
        BudgetStaffCostEntry(
            2,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            1,
            BigDecimal.ONE,
            emptySet(),
            emptySet(),
            emptySet(),
        )
    )

    private fun budgetTravelAndAccommodationCostEntries() = listOf(
        BudgetTravelAndAccommodationCostEntry(
            1,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            null,
            BigDecimal.ONE,
            emptySet(),
            emptySet()
        ),
        BudgetTravelAndAccommodationCostEntry(
            2,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            2,
            BigDecimal.ONE,
            emptySet(),
            emptySet(),
        )
    )

    private fun budgetSpfCostEntries() = listOf(
        BudgetSpfCostEntry(
            1,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            null,
            BigDecimal.ONE,
            emptySet(),
            emptySet()
        ),
        BudgetSpfCostEntry(
            2,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            2,
            BigDecimal.ONE,
            emptySet(),
            emptySet(),
        )
    )

    private fun budgetGeneralCostEntries() = listOf(
        BudgetGeneralCostEntry(
            1,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            null,
            BigDecimal.ONE,
            0L,
            emptySet(),
            emptySet(),
            emptySet()
        ),
        BudgetGeneralCostEntry(
            2,
            BigDecimal.ONE,
            BigDecimal.ONE,
            mutableSetOf(),
            3,
            BigDecimal.ONE,
            0L,
            emptySet(),
            emptySet(),
            emptySet(),
        )
    )

    private fun budgetUnitCostEntries() = listOf(
        BudgetUnitCostEntry(1, BigDecimal.ONE, mutableSetOf(), BigDecimal.ONE, 1)
    )

}
