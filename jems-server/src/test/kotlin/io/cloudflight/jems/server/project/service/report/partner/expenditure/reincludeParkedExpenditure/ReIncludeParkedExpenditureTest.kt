package io.cloudflight.jems.server.project.service.report.partner.expenditure.reincludeParkedExpenditure

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.report.model.file.JemsFile
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileCreate
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileType
import io.cloudflight.jems.server.project.service.report.model.file.UserSimple
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ExpenditureParkingMetadata
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportExpenditureCost
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.control.expenditure.PartnerReportParkedExpenditurePersistence
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import io.cloudflight.jems.server.project.service.report.partner.expenditure.deleteParkedExpenditure.DeleteParkedExpenditureTest.Companion.report
import io.cloudflight.jems.server.project.service.report.partner.file.ProjectPartnerReportFilePersistence
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
import org.springframework.context.ApplicationEventPublisher
import java.time.ZonedDateTime

internal class ReIncludeParkedExpenditureTest : UnitTest() {

    companion object {
        private val time = ZonedDateTime.now()
    }

    @MockK
    private lateinit var reportParkedExpenditurePersistence: PartnerReportParkedExpenditurePersistence
    @MockK
    private lateinit var reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence
    @MockK
    private lateinit var reportFilePersistence: ProjectPartnerReportFilePersistence
    @MockK
    private lateinit var reportPersistence: ProjectPartnerReportPersistence
    @MockK
    private lateinit var filePersistence: JemsFilePersistence
    @MockK
    private lateinit var partnerPersistence: PartnerPersistence
    @MockK
    private lateinit var auditPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var interactor: ReIncludeParkedExpenditure

    @BeforeEach
    fun reset() {
        clearMocks(reportFilePersistence)
    }

    @Test
    fun `reIncludeParkedExpenditure - without attachment`() {
        every { reportExpenditurePersistence.getExpenditureAttachment(4L, 400L) } returns null
        val mockedResult = mockk<ProjectPartnerReportExpenditureCost>()
        every { mockedResult.parkingMetadata } returns
            ExpenditureParkingMetadata(reportOfOriginId = 75L, reportOfOriginNumber = 10, originalExpenditureNumber = 11)
        every { reportExpenditurePersistence.reIncludeParkedExpenditure(4L, 40L, 400L) } returns mockedResult
        every { reportParkedExpenditurePersistence.unParkExpenditures(setOf(400L)) } answers { }
        every { partnerPersistence.getProjectIdForPartnerId(id = 4L) } returns 8L

        val report = report(reportId = 40L)
        every { reportPersistence.getPartnerReportById(partnerId = 4L, reportId = 40L) } returns report

        val slotAudit = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(slotAudit)) } answers {}

        interactor.reIncludeParkedExpenditure(partnerId = 4L, reportId = 40L, expenditureId = 400L)

        verify(exactly = 1) { reportParkedExpenditurePersistence.unParkExpenditures(setOf(400L)) }
        verify(exactly = 0) { reportFilePersistence.updatePartnerReportExpenditureAttachment(any(), any()) }

        assertThat(slotAudit.captured.auditCandidate).isEqualTo(AuditCandidate(
            action = AuditAction.PARKED_EXPENDITURE_REINCLUDED,
            project = AuditProject(id = "8", customIdentifier = "PROJ-TEST", name = "project test"),
            entityRelatedId = 40L,
            description = "Parked expenditure R10.11 was re-included in partner report R.16 by partner PP7",
        ))
    }

    @Test
    fun `reIncludeParkedExpenditure - with attachment`() {
        val file = JemsFile(
            id = 275L,
            name = "powerpoint.pptx",
            type = JemsFileType.Expenditure,
            uploaded = time,
            author = UserSimple(id = 318L, "email", name = "name", surname = "surname"),
            size = 324L,
            description = "desc"
        )
        every { reportExpenditurePersistence.getExpenditureAttachment(5L, 500L) } returns file

        val newExpenditure = mockk<ProjectPartnerReportExpenditureCost>()
        every { newExpenditure.id } returns 945L
        every { newExpenditure.parkingMetadata } returns
            ExpenditureParkingMetadata(reportOfOriginId = 77L, reportOfOriginNumber = 3, originalExpenditureNumber = 4)
        every { reportExpenditurePersistence.reIncludeParkedExpenditure(5L, 50L, 500L) } returns newExpenditure
        every { reportParkedExpenditurePersistence.unParkExpenditures(setOf(500L)) } answers { }
        every { partnerPersistence.getProjectIdForPartnerId(5L) } returns 55L

        val testContent = ByteArray(5)
        every { filePersistence.downloadFile(5L, 275L) } returns Pair("", testContent)
        val fileSlot = slot<JemsFileCreate>()
        every { reportFilePersistence.updatePartnerReportExpenditureAttachment(945L, capture(fileSlot)) } returns mockk()

        val report = report(reportId = 50L)
        every { reportPersistence.getPartnerReportById(partnerId = 5L, reportId = 50L) } returns report

        val slotAudit = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(slotAudit)) } answers {}

        interactor.reIncludeParkedExpenditure(partnerId = 5L, reportId = 50L, expenditureId = 500L)

        verify(exactly = 1) { reportParkedExpenditurePersistence.unParkExpenditures(setOf(500L)) }
        verify(exactly = 1) { reportFilePersistence.updatePartnerReportExpenditureAttachment(any(), any()) }

        with(fileSlot.captured) {
            assertThat(projectId).isEqualTo(55L)
            assertThat(partnerId).isEqualTo(5L)
            assertThat(name).isEqualTo("powerpoint.pptx")
            assertThat(path).isEqualTo("Project/000055/Report/Partner/000005/PartnerReport/000050/Expenditure/000945/")
            assertThat(type).isEqualTo(JemsFileType.Expenditure)
            assertThat(size).isEqualTo(324L)
            assertThat(content.readAllBytes()).isEqualTo(testContent)
            assertThat(userId).isEqualTo(318L)
        }
    }
}
