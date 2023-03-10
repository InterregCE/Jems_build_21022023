package io.cloudflight.jems.server.project.service.report.partner.expenditure.getAvailableUnitCostsForReport

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportUnitCost
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ReportBudgetCategory
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class GetAvailableUnitCostsForReportTest : UnitTest() {
    private val PARTNER_ID = 466L

    private val unitCostZero = ProjectPartnerReportUnitCost(
        id = 1L,
        unitCostProgrammeId = 45L,
        projectDefined = false,
        numberOfUnits = BigDecimal.ZERO,
        total = BigDecimal.ZERO,
        name = setOf(InputTranslation(SystemLanguage.EN, "first EN")),
        category = ReportBudgetCategory.Multiple,
        foreignCurrencyCode = null,
        costPerUnitForeignCurrency = null,
        costPerUnit = BigDecimal.ZERO
    )

    private val unitCostNonZero = ProjectPartnerReportUnitCost(
        id = 2L,
        unitCostProgrammeId = 45L,
        projectDefined = true,
        numberOfUnits = BigDecimal.ONE,
        total = BigDecimal.ONE,
        name = setOf(InputTranslation(SystemLanguage.EN, "second EN")),
        category = ReportBudgetCategory.Multiple,
        foreignCurrencyCode = null,
        costPerUnitForeignCurrency = null,
        costPerUnit = BigDecimal.ONE
    )

    @MockK
    lateinit var reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence

    @InjectMockKs
    lateinit var interactor: GetAvailableUnitCostsForReport

    @Test
    fun getUnitCosts() {
        every { reportExpenditurePersistence.getAvailableUnitCosts(PARTNER_ID, 10L) } returns
            listOf(unitCostZero, unitCostNonZero)
        Assertions.assertThat(interactor.getUnitCosts(PARTNER_ID, 10L)).containsExactly(unitCostNonZero)
    }
}
