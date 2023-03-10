package io.cloudflight.jems.server.project.service.report.partner.expenditure.getAvailableLumpSumsForReport

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportLumpSum
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class GetAvailableLumpSumsForReportTest : UnitTest() {

    private val PARTNER_ID = 466L

    private val lumpSumZero = ProjectPartnerReportLumpSum(
        id = 1L,
        lumpSumProgrammeId = 45L,
        fastTrack = false,
        orderNr = 11,
        period = null,
        cost = BigDecimal.valueOf(0, 1),
        name = setOf(InputTranslation(SystemLanguage.EN, "first EN")),
    )
    private val lumpSumNonZero = ProjectPartnerReportLumpSum(
        id = 2L,
        lumpSumProgrammeId = 46L,
        fastTrack = false,
        orderNr = 12,
        period = null,
        cost = BigDecimal.valueOf(10, 1),
        name = setOf(InputTranslation(SystemLanguage.EN, "second EN")),
    )
    private val lumpSumFastTrack = ProjectPartnerReportLumpSum(
        id = 3L,
        lumpSumProgrammeId = 47L,
        fastTrack = true,
        orderNr = 13,
        period = null,
        cost = BigDecimal.valueOf(101, 2),
        name = setOf(InputTranslation(SystemLanguage.EN, "third EN")),
    )

    @MockK
    lateinit var reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence

    @InjectMockKs
    lateinit var interactor: GetAvailableLumpSumsForReport

    @Test
    fun getLumpSums() {
        every { reportExpenditurePersistence.getAvailableLumpSums(PARTNER_ID, 10L) } returns
            listOf(lumpSumZero, lumpSumNonZero, lumpSumFastTrack)
        assertThat(interactor.getLumpSums(PARTNER_ID, 10L)).containsExactly(lumpSumNonZero)
    }

}
