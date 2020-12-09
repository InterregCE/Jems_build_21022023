package io.cloudflight.jems.server.programme.service.costoption.update_unit_cost

import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.OfficeAndAdministrationCosts
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.StaffCosts
import io.cloudflight.jems.server.audit.entity.AuditAction
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.common.exception.I18nFieldError
import io.cloudflight.jems.server.common.exception.I18nValidationException
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.service.costoption.ProgrammeUnitCostPersistence
import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeUnitCost
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.stream.Collectors

class UpdateUnitCostInteractorTest {

    @MockK
    lateinit var persistence: ProgrammeUnitCostPersistence

    @MockK
    lateinit var auditService: AuditService

    private lateinit var updateUnitCostInteractor: UpdateUnitCostInteractor

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        updateUnitCostInteractor = UpdateUnitCost(persistence, auditService)
    }

    @Test
    fun `update unit cost - test if various invalid values will fail`() {
        val wrongUnitCost = ProgrammeUnitCost(
            id = 4,
            name = " ",
            description = "test unit cost 1",
            type = null,
            costPerUnit = null,
            categories = setOf(OfficeAndAdministrationCosts),
        )
        val ex = assertThrows<I18nValidationException> { updateUnitCostInteractor.updateUnitCost(wrongUnitCost) }
        assertThat(ex.i18nFieldErrors).containsExactlyInAnyOrderEntriesOf(mapOf(
            "name" to I18nFieldError(i18nKey = "programme.unitCost.name.should.not.be.empty"),
            "costPerUnit" to I18nFieldError(i18nKey = "programme.unitCost.costPerUnit.invalid"),
            "type" to I18nFieldError(i18nKey = "programme.unitCost.type.should.not.be.empty"),
            "categories" to I18nFieldError(i18nKey = "programme.unitCost.categories.min.2"),
        ))
    }

    @Test
    fun `update unit cost - test if longer strings than allowed will fail`() {
        val wrongUnitCost = ProgrammeUnitCost(
            id = 4,
            name = getStringOfLength(51),
            description = getStringOfLength(501),
            type = getStringOfLength(26),
            costPerUnit = BigDecimal.ONE,
            categories = setOf(StaffCosts, OfficeAndAdministrationCosts),
        )
        val ex = assertThrows<I18nValidationException> { updateUnitCostInteractor.updateUnitCost(wrongUnitCost) }
        assertThat(ex.i18nFieldErrors).containsExactlyInAnyOrderEntriesOf(mapOf(
            "name" to I18nFieldError(i18nKey = "programme.unitCost.name.too.long"),
            "description" to I18nFieldError(i18nKey = "programme.unitCost.description.too.long"),
            "type" to I18nFieldError(i18nKey = "programme.unitCost.type.too.long"),
        ))
    }

    @Test
    fun `update unit cost - test if valid UnitCost is properly saved`() {
        every { persistence.updateUnitCost(any()) } returnsArgument 0
        val unitCost = ProgrammeUnitCost(
            id = 4,
            name = "UC1",
            description = "test unit cost 1",
            type = "test type 1",
            costPerUnit = BigDecimal.ONE,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
        )
        val auditSlot = slot<AuditCandidate>()
        every { auditService.logEvent(capture(auditSlot)) } answers {}
        assertThat(updateUnitCostInteractor.updateUnitCost(unitCost)).isEqualTo(unitCost.copy())
        assertThat(auditSlot.captured).isEqualTo(AuditCandidate(
            action = AuditAction.PROGRAMME_UNIT_COST_CHANGED,
            description = "Programme unit cost (id=4) 'UC1' has been changed"
        ))
    }

    @Test
    fun `update unit cost - test if validation will fail when wrong ID is filled in`() {
        val unitCost = ProgrammeUnitCost(
            name = "UC1",
            costPerUnit = BigDecimal.ONE,
        )

        assertThrows<I18nValidationException>("when updating id cannot be invalid") {
            updateUnitCostInteractor.updateUnitCost(unitCost.copy(id = 0)) }
        assertThrows<I18nValidationException>("when updating id cannot be invalid") {
            updateUnitCostInteractor.updateUnitCost(unitCost.copy(id = null)) }
    }

    @Test
    fun `update unit cost - test if not existing UnitCost will fail with correct exception`() {
        val unitCost = ProgrammeUnitCost(
            id = 777,
            name = "UC1",
            type = "UC1 type",
            costPerUnit = BigDecimal.ONE,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
        )
        every { persistence.updateUnitCost(any()) } throws ResourceNotFoundException("programmeUnitCost")

        assertThrows<ResourceNotFoundException>("when updating not existing unit cost") {
            updateUnitCostInteractor.updateUnitCost(unitCost) }
    }

    private fun getStringOfLength(length: Int): String =
        IntArray(length).map { "x" }.stream().collect(Collectors.joining())

}