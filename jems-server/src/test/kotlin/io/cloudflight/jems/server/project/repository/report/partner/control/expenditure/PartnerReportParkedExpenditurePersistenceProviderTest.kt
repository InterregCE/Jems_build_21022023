package io.cloudflight.jems.server.project.repository.report.partner.control.expenditure

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.entity.report.control.expenditure.PartnerReportParkedExpenditureEntity
import io.cloudflight.jems.server.project.entity.report.partner.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportExpenditureCostEntity
import io.cloudflight.jems.server.project.repository.report.partner.ProjectPartnerReportRepository
import io.cloudflight.jems.server.project.repository.report.partner.expenditure.ProjectPartnerReportExpenditureRepository
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.model.partner.control.expenditure.ParkExpenditureData
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ExpenditureParkingMetadata
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PartnerReportParkedExpenditurePersistenceProviderTest : UnitTest() {

    @MockK
    private lateinit var reportRepository: ProjectPartnerReportRepository
    @MockK
    private lateinit var reportExpenditureRepository: ProjectPartnerReportExpenditureRepository
    @MockK
    private lateinit var reportParkedExpenditureRepository: PartnerReportParkedExpenditureRepository

    @InjectMockKs
    private lateinit var persistence: PartnerReportParkedExpenditurePersistenceProvider

    @BeforeEach
    fun reset() {
        clearMocks(reportRepository, reportExpenditureRepository, reportParkedExpenditureRepository)
    }

    @Test
    fun getParkedExpendituresByIdForPartner() {
        val originalReport = mockk<ProjectPartnerReportEntity>()
        every { originalReport.id } returns 40L
        every { originalReport.number } returns 401

        val expenditure = PartnerReportParkedExpenditureEntity(parkedFromExpenditureId = 5499L, mockk(), originalReport, 21)
        every { reportParkedExpenditureRepository
            .findAllByParkedFromPartnerReportPartnerIdAndParkedFromPartnerReportStatus(
                partnerId = 12L,
                status = ReportStatus.Certified,
            )
        } returns listOf(expenditure)

        assertThat(persistence.getParkedExpendituresByIdForPartner(partnerId = 12L, ReportStatus.Certified))
            .containsExactlyEntriesOf(mapOf(5499L to ExpenditureParkingMetadata(40L, 401, 21)))
    }

    @Test
    fun parkExpenditures() {
        val slotSaved = slot<Iterable<PartnerReportParkedExpenditureEntity>>()
        every { reportParkedExpenditureRepository.saveAll(capture(slotSaved)) } returnsArgument 0

        val expenditure = mockk<PartnerReportExpenditureCostEntity>()
        every { reportExpenditureRepository.getById(24L) } returns expenditure
        val report = mockk<ProjectPartnerReportEntity>()
        every { reportRepository.getById(444L) } returns report

        val toPark = setOf(
            ParkExpenditureData(expenditureId = 24L, originalReportId = 444L, originalNumber = 7)
        )
        persistence.parkExpenditures(toPark)

        assertThat(slotSaved.captured).hasSize(1)
        with(slotSaved.captured.first()) {
            assertThat(parkedFromExpenditureId).isEqualTo(24L)
            assertThat(parkedFrom).isEqualTo(expenditure)
            assertThat(reportOfOrigin).isEqualTo(report)
            assertThat(originalNumber).isEqualTo(7)
        }
    }

    @Test
    fun unParkExpenditures() {
        val slotDeletedIds = slot<Collection<Long>>()
        every { reportParkedExpenditureRepository.deleteAllById(capture(slotDeletedIds)) } answers { }

        persistence.unParkExpenditures(setOf(4L, 7L, 43L))

        assertThat(slotDeletedIds.captured).containsExactlyInAnyOrder(4L, 7L, 43L)
    }

}
