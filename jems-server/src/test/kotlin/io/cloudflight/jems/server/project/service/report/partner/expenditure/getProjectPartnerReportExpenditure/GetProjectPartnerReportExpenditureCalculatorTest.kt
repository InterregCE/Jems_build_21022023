package io.cloudflight.jems.server.project.service.report.partner.expenditure.getProjectPartnerReportExpenditure

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.currency.repository.CurrencyPersistence
import io.cloudflight.jems.server.currency.service.model.CurrencyConversion
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportExpenditureCost
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ReportBudgetCategory
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileMetadata
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ExpenditureParkingMetadata
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

internal class GetProjectPartnerReportExpenditureCalculatorTest : UnitTest() {

    companion object {
        private const val PARTNER_ID = 520L
        private val TODAY = LocalDate.now()
        private val YEAR = LocalDate.now().year
        private val MONTH = LocalDate.now().monthValue
        private val MOMENT = ZonedDateTime.now()

        private fun expenditure(id: Long) = ProjectPartnerReportExpenditureCost(
            id = id,
            number = 1,
            lumpSumId = 45L,
            unitCostId = 46L,
            costCategory = ReportBudgetCategory.TravelAndAccommodationCosts,
            investmentId = 89L,
            contractId = 54L,
            internalReferenceNumber = "145",
            invoiceNumber = "1",
            invoiceDate = TODAY,
            dateOfPayment = TODAY,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.ZERO,
            declaredAmount = BigDecimal.valueOf(2431, 2),
            currencyCode = "CST",
            currencyConversionRate = null,
            declaredAmountAfterSubmission = null,
            attachment = JemsFileMetadata(45L, "file.txt", MOMENT),
            parkingMetadata = null,
        )

        private fun filledInExpenditure(id: Long) = ProjectPartnerReportExpenditureCost(
            id = id,
            number = 1,
            lumpSumId = 45L,
            unitCostId = 46L,
            costCategory = ReportBudgetCategory.TravelAndAccommodationCosts,
            investmentId = 89L,
            contractId = 54L,
            internalReferenceNumber = "145",
            invoiceNumber = "1",
            invoiceDate = TODAY,
            dateOfPayment = TODAY,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.ZERO,
            declaredAmount = BigDecimal.valueOf(2431, 2),
            currencyCode = "CST",
            currencyConversionRate = BigDecimal.valueOf(24302, 4),
            declaredAmountAfterSubmission = BigDecimal.valueOf(1000, 2),
            attachment = JemsFileMetadata(45L, "file.txt", MOMENT),
            parkingMetadata = null,
        )

        private val cstCurrency = CurrencyConversion(
            code = "CST",
            year = YEAR,
            month = MONTH,
            name = "Custom Currency",
            conversionRate = BigDecimal.valueOf(24302, 4),
        )
    }

    @MockK
    lateinit var reportPersistence: ProjectPartnerReportPersistence

    @MockK
    lateinit var reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence

    @MockK
    lateinit var currencyPersistence: CurrencyPersistence

    @InjectMockKs
    lateinit var calculator: GetProjectPartnerReportExpenditureCalculator

    @BeforeEach
    fun reset() {
        clearMocks(currencyPersistence)
    }

    @ParameterizedTest(name = "getContribution and fill in currencies (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Draft"])
    fun `getContribution and fill in`(status: ReportStatus) {
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns status
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 74L) } returns report

        every { reportExpenditurePersistence.getPartnerReportExpenditureCosts(PARTNER_ID, reportId = 74L) } returns
            listOf(expenditure(10L))

        every { currencyPersistence.findAllByIdYearAndIdMonth(year = YEAR, month = MONTH) } returns listOf(cstCurrency)

        assertThat(calculator.getExpenditureCosts(PARTNER_ID, reportId = 74L))
            .containsExactly(filledInExpenditure(10L))
    }

    @Test
    fun `getContribution and fill in, but not parked ones`() {
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.Draft
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 76L) } returns report

        val parked_11 = mockk<ExpenditureParkingMetadata>()
        every { reportExpenditurePersistence.getPartnerReportExpenditureCosts(PARTNER_ID, reportId = 76L) } returns listOf(
            expenditure(10L)
                .copy(currencyConversionRate = BigDecimal.ONE, declaredAmountAfterSubmission = BigDecimal.TEN),
            expenditure(11L)
                .copy(currencyConversionRate = BigDecimal.ONE, declaredAmountAfterSubmission = BigDecimal.TEN, parkingMetadata = parked_11)
        )

        every { currencyPersistence.findAllByIdYearAndIdMonth(year = YEAR, month = MONTH) } returns emptyList()

        assertThat(calculator.getExpenditureCosts(PARTNER_ID, reportId = 76L)).containsExactly(
            filledInExpenditure(10L).copy(currencyConversionRate = null, declaredAmountAfterSubmission = null),
            filledInExpenditure(11L).copy(
                currencyConversionRate = BigDecimal.ONE,
                declaredAmountAfterSubmission = BigDecimal.valueOf(2431L, 2),
                parkingMetadata = parked_11,
            ),
        )
    }

    @ParameterizedTest(name = "getContribution and NOT fill in currencies (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Draft"], mode = EnumSource.Mode.EXCLUDE)
    fun `getContribution and NOT fill in`(status: ReportStatus) {
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns status
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 78L) } returns report

        val notFilledInExpenditure = expenditure(15L)
        every { reportExpenditurePersistence.getPartnerReportExpenditureCosts(PARTNER_ID, reportId = 78L) } returns
            listOf(notFilledInExpenditure)

        assertThat(calculator.getExpenditureCosts(PARTNER_ID, reportId = 78L))
            .containsExactly(notFilledInExpenditure)

        verify(exactly = 0) { currencyPersistence.getConversionForCodeAndMonth(any(), any(), any()) }
    }

    @Test
    fun `test rounding`() {
        val notFilledInExpenditure = expenditure(30L)
        notFilledInExpenditure.fillInRate(BigDecimal.valueOf(24305, 4))

        assertThat(notFilledInExpenditure.declaredAmountAfterSubmission)
            .isEqualTo(BigDecimal.valueOf(1000, 2))
    }

}
