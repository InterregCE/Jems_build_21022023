package io.cloudflight.jems.server.project.service.report.partner.expenditure.deleteParkedExpenditure

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.cloudflight.jems.server.project.service.report.model.partner.PartnerReportIdentification
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ExpenditureParkingMetadata
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.control.expenditure.PartnerReportParkedExpenditurePersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.ZonedDateTime

internal class DeleteParkedExpenditureTest : UnitTest() {

    companion object {
        fun report(reportId: Long): ProjectPartnerReport {
            val identification = mockk<PartnerReportIdentification>()
            every { identification.projectIdentifier } returns "PROJ-TEST"
            every { identification.projectAcronym } returns "project test"
            every { identification.partnerRole } returns ProjectPartnerRole.PARTNER
            every { identification.partnerNumber } returns 7
            return  ProjectPartnerReport(
                id = reportId,
                reportNumber = 16,
                status = ReportStatus.Draft,
                version = "V6",
                firstSubmission = ZonedDateTime.now(),
                identification = identification,
            )
        }
    }

    @MockK
    private lateinit var reportParkedExpenditurePersistence: PartnerReportParkedExpenditurePersistence
    @MockK
    private lateinit var reportPersistence: ProjectPartnerReportPersistence
    @MockK
    private lateinit var partnerPersistence: PartnerPersistence
    @MockK
    private lateinit var auditPublisher: ApplicationEventPublisher

    @InjectMockKs
    lateinit var interactor: DeleteParkedExpenditure

    @BeforeEach
    fun reset() {
        clearMocks(reportParkedExpenditurePersistence, reportPersistence, partnerPersistence, auditPublisher)
    }

    @Test
    fun deleteParkedExpenditure() {
        val partnerId = 47L
        val reportId = 470L
        val expenditure = ExpenditureParkingMetadata(reportOfOriginId = 348L, reportOfOriginNumber = 1, originalExpenditureNumber = 2)
        every { reportParkedExpenditurePersistence.getParkedExpendituresByIdForPartner(partnerId, ReportStatus.Certified) } returns
            mapOf(845L to expenditure)
        every { reportParkedExpenditurePersistence.unParkExpenditures(setOf(845L)) } answers { }

        val report = report(reportId)
        every { reportPersistence.getPartnerReportById(partnerId = partnerId, reportId = reportId) } returns report
        every { partnerPersistence.getProjectIdForPartnerId(id = partnerId, "V6") } returns 2L

        val slotAudit = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(slotAudit)) } answers {}

        interactor.deleteParkedExpenditure(partnerId, reportId = reportId, expenditureId = 845L)
        verify(exactly = 1) { reportParkedExpenditurePersistence.unParkExpenditures(setOf(845L)) }

        assertThat(slotAudit.captured.auditCandidate).isEqualTo(
            AuditCandidate(
                action = AuditAction.PARKED_EXPENDITURE_DELETED,
                project = AuditProject(id = "2", customIdentifier = "PROJ-TEST", name = "project test"),
                entityRelatedId = 470L,
                description = "Parked expenditure R1.2 was deleted in partner report R.16 by partner PP7",
            )
        )
    }

    @Test
    fun `deleteParkedExpenditure - not existing`() {
        every { reportParkedExpenditurePersistence.getParkedExpendituresByIdForPartner(48L, ReportStatus.Certified) } returns emptyMap()
        assertThrows<ParkedExpenditureNotFound> {
            interactor.deleteParkedExpenditure(48L, reportId = 250L, expenditureId = -1L)
        }
        verify(exactly = 0) { reportParkedExpenditurePersistence.unParkExpenditures(any()) }
    }

}
