package io.cloudflight.jems.server.project.service.partner.budget.get_budget_total_cost

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.budget.model.BudgetCostsCalculationResult
import io.cloudflight.jems.server.project.service.common.BudgetCostsCalculatorService
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetPersistence
import io.cloudflight.jems.server.project.service.partner.budget.get_budget_options.GetBudgetOptionsInteractor
import io.cloudflight.jems.server.project.service.partner.budget.percentage
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerBudgetOptions
import io.cloudflight.jems.server.toScaledBigDecimal
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class GetBudgetTotalCostTest : UnitTest() {

    val partnerId = 1L
    private val staffCostTotal = 1_324_500.0.toScaledBigDecimal()
    private val travelCostTotal = 1_160_040.toScaledBigDecimal()
    private val equipmentCostTotal = 321.toScaledBigDecimal()
    private val externalCostTotal = 662.25.toScaledBigDecimal()
    private val infrastructureCostTotal = 773.36.toScaledBigDecimal()
    private val unitCostTotal = 563.36.toScaledBigDecimal()
    private val lumpSumsTotal = 123.4.toScaledBigDecimal()

    @MockK
    lateinit var persistence: ProjectPartnerBudgetPersistence

    @MockK
    lateinit var getBudgetOptions: GetBudgetOptionsInteractor

    @MockK
    lateinit var budgetCostsCalculatorService: BudgetCostsCalculatorService

    @InjectMockKs
    lateinit var getBudgetTotalCost: GetBudgetTotalCost

    @BeforeAll
    fun setup() {

        every { persistence.getBudgetStaffCostTotal(partnerId) } returns staffCostTotal
        every { persistence.getBudgetEquipmentCostTotal(partnerId) } returns equipmentCostTotal
        every { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) } returns externalCostTotal
        every { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) } returns infrastructureCostTotal
        every { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) } returns travelCostTotal
        every { persistence.getBudgetUnitCostTotal(partnerId) } returns unitCostTotal
        every { persistence.getBudgetLumpSumsCostTotal(partnerId) } returns lumpSumsTotal
    }

    @Test
    fun `should return sum of budget cost entries for the specified partner when budgetOptions is null `() {

        every { getBudgetOptions.getBudgetOptions(partnerId) } returns null
        every {
            budgetCostsCalculatorService.calculateCosts(
                null,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        } returns BudgetCostsCalculationResult(staffCostTotal, travelCostTotal, BigDecimal.ZERO, BigDecimal.ZERO)


        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetStaffCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                null,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                staffCostTotal,
                travelCostTotal,
                equipmentCostTotal,
                externalCostTotal,
                infrastructureCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries for the specified partner when no flat rate is set in the budgetOptions`() {
        val budgetOptions = newBudgetOptionsInstance(partnerId = partnerId)

        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions
        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        } returns BudgetCostsCalculationResult(staffCostTotal, travelCostTotal, BigDecimal.ZERO, BigDecimal.ZERO)

        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetStaffCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                staffCostTotal,
                travelCostTotal,
                equipmentCostTotal,
                externalCostTotal,
                infrastructureCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries and flat rate costs for the specified partner when travelAndAccommodationOnStaffCostsFlatRate is set in the budgetOptions`() {

        val budgetOptions =
            newBudgetOptionsInstance(partnerId = partnerId, travelAndAccommodationOnStaffCostsFlatRate = 10)
        val expectedTravelCostTotal =
            staffCostTotal.percentage(budgetOptions.travelAndAccommodationOnStaffCostsFlatRate!!)
        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions
        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                BigDecimal.ZERO,
                staffCostTotal
            )
        } returns BudgetCostsCalculationResult(
            staffCostTotal,
            expectedTravelCostTotal,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        )

        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetStaffCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                BigDecimal.ZERO,
                staffCostTotal
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                expectedTravelCostTotal,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                staffCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries and flat rate costs for the specified partner when staffCostsFlatRate is set in the budgetOptions`() {

        val budgetOptions = newBudgetOptionsInstance(partnerId = partnerId, staffCostsFlatRate = 15)
        val expectedStaffCostTotal = sumOf(
            travelCostTotal,
            externalCostTotal,
            equipmentCostTotal,
            infrastructureCostTotal
        ).percentage(budgetOptions.staffCostsFlatRate!!)
        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions

        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                BigDecimal.ZERO
            )
        } returns BudgetCostsCalculationResult(
            expectedStaffCostTotal,
            travelCostTotal,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        )

        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                BigDecimal.ZERO
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                travelCostTotal,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                expectedStaffCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries and flat rate costs for the specified partner when staffCostsFlatRate and travelAndAccommodationOnStaffCostsFlatRate are set in the budgetOptions`() {

        val budgetOptions = newBudgetOptionsInstance(
            partnerId = partnerId,
            travelAndAccommodationOnStaffCostsFlatRate = 10,
            staffCostsFlatRate = 20
        )
        val expectedStaffCostTotal = sumOf(
            externalCostTotal,
            equipmentCostTotal,
            infrastructureCostTotal
        ).percentage(budgetOptions.staffCostsFlatRate!!)
        val expectedTravelCostTotal =
            expectedStaffCostTotal.percentage(budgetOptions.travelAndAccommodationOnStaffCostsFlatRate!!)
        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions

        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            )
        } returns BudgetCostsCalculationResult(
            expectedStaffCostTotal,
            expectedTravelCostTotal,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        )

        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                expectedStaffCostTotal,
                expectedTravelCostTotal,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries and flat rate costs when officeAndAdministrationOnStaffCostsFlatRate is set in the budgetOptions`() {

        val budgetOptions =
            newBudgetOptionsInstance(partnerId = partnerId, officeAndAdministrationOnStaffCostsFlatRate = 10)
        val expectedOfficeCostTotal =
            staffCostTotal.percentage(budgetOptions.officeAndAdministrationOnStaffCostsFlatRate!!)
        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions

        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        } returns BudgetCostsCalculationResult(
            staffCostTotal,
            travelCostTotal,
            expectedOfficeCostTotal,
            BigDecimal.ZERO
        )


        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetStaffCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                expectedOfficeCostTotal,
                travelCostTotal,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                staffCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    @Test
    fun `should return sum of budget cost entries and flat rate costs for the specified partner when otherCostsOnStaffCostsFlatRate is set in the budgetOptions`() {

        val budgetOptions = newBudgetOptionsInstance(partnerId = partnerId, otherCostsOnStaffCostsFlatRate = 30)
        val expectedOtherCostTotal = staffCostTotal.percentage(budgetOptions.otherCostsOnStaffCostsFlatRate!!)
        every { getBudgetOptions.getBudgetOptions(partnerId) } returns budgetOptions

        every {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        } returns BudgetCostsCalculationResult(staffCostTotal, travelCostTotal, BigDecimal.ZERO, expectedOtherCostTotal)

        val result = getBudgetTotalCost.getBudgetTotalCost(partnerId)

        verify(atLeast = 1) { getBudgetOptions.getBudgetOptions(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetStaffCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetTravelAndAccommodationCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetEquipmentCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetExternalExpertiseAndServicesCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetInfrastructureAndWorksCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetUnitCostTotal(partnerId) }
        verify(atLeast = 1) { persistence.getBudgetLumpSumsCostTotal(partnerId) }
        verify(atLeast = 1) {
            budgetCostsCalculatorService.calculateCosts(
                budgetOptions,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                travelCostTotal,
                staffCostTotal
            )
        }
        confirmVerified(persistence, budgetCostsCalculatorService)

        assertEquals(
            sumOf(
                expectedOtherCostTotal,
                travelCostTotal,
                externalCostTotal,
                equipmentCostTotal,
                infrastructureCostTotal,
                staffCostTotal,
                unitCostTotal,
                lumpSumsTotal
            ), result
        )
    }

    private fun newBudgetOptionsInstance(
        partnerId: Long = 1L,
        officeAndAdministrationOnStaffCostsFlatRate: Int? = null,
        travelAndAccommodationOnStaffCostsFlatRate: Int? = null,
        staffCostsFlatRate: Int? = null,
        otherCostsOnStaffCostsFlatRate: Int? = null
    ) =
        ProjectPartnerBudgetOptions(
            partnerId,
            officeAndAdministrationOnStaffCostsFlatRate,
            travelAndAccommodationOnStaffCostsFlatRate,
            staffCostsFlatRate,
            otherCostsOnStaffCostsFlatRate
        )

    private fun sumOf(vararg values: BigDecimal) = values.reduce { acc, value -> acc.plus(value) }
}
