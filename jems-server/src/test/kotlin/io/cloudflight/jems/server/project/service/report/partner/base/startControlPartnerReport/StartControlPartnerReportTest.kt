package io.cloudflight.jems.server.project.service.report.partner.base.startControlPartnerReport

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.controllerInstitution.service.model.ControllerInstitutionList
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReportSubmissionSummary
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.model.partner.control.overview.ControlOverview
import io.cloudflight.jems.server.project.service.report.partner.control.overview.ProjectPartnerReportControlOverviewPersistence
import io.cloudflight.jems.server.project.service.report.partner.identification.ProjectPartnerReportDesignatedControllerPersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import java.time.ZonedDateTime

internal class StartControlPartnerReportTest : UnitTest() {

    companion object {
        private const val PROJECT_ID = 256L
        private const val PARTNER_ID = 581L

        private val mockedResult = ProjectPartnerReportSubmissionSummary(
            id = 37L,
            reportNumber = 4,
            status = ReportStatus.InControl,
            version = "5.6.1",
            // not important
            firstSubmission = ZonedDateTime.now(),
            controlEnd = null,
            createdAt = ZonedDateTime.now(),
            projectIdentifier = "FG01_654",
            projectAcronym = "acronym",
            partnerNumber = 1,
            partnerRole = ProjectPartnerRole.LEAD_PARTNER,
        )

        private val controllerInstitution = ControllerInstitutionList(
            id = 1,
            name = "Test institution",
            description = null,
            institutionNuts = emptyList(),
            createdAt = ZonedDateTime.now()
        )

        private val controlOverview = ControlOverview(
            startDate = LocalDate.now(),
            requestsForClarifications = "test",
            receiptOfSatisfactoryAnswers = "test answers",
            endDate = null,
            findingDescription = null,
            followUpMeasuresFromLastReport = "test from last report",
            conclusion = "result",
            followUpMeasuresForNextReport = null
        )

    }

    @MockK
    lateinit var reportPersistence: ProjectPartnerReportPersistence

    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @MockK
    lateinit var controlInstitutionPersistence: ControllerInstitutionPersistence

    @MockK
    lateinit var reportDesignatedControllerPersistence: ProjectPartnerReportDesignatedControllerPersistence

    @MockK
    lateinit var controlOverviewPersistence: ProjectPartnerReportControlOverviewPersistence

    @MockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @InjectMockKs
    lateinit var interactor: StartControlPartnerReport

    @BeforeEach
    fun reset() {
        clearMocks(reportPersistence)
        clearMocks(partnerPersistence)
        clearMocks(auditPublisher)
        clearMocks(controlInstitutionPersistence)
        clearMocks(reportDesignatedControllerPersistence)
    }

    @ParameterizedTest(name = "startControl (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Submitted"])
    fun startControl(status: ReportStatus) {
        val report = report(37L, status)
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 37L) } returns report
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID, "5.6.1") } returns PROJECT_ID
        every { reportPersistence.startControlOnReportById(any(), any()) } returns mockedResult
        every { controlInstitutionPersistence.getControllerInstitutions(setOf(PARTNER_ID))} returns mapOf(Pair(PARTNER_ID, controllerInstitution))
        every { reportDesignatedControllerPersistence.create(PARTNER_ID, 37L, controllerInstitution.id)} returns Unit
        every { reportPersistence.getLastCertifiedPartnerReportId(PARTNER_ID)} returns 5
        every { controlOverviewPersistence.createPartnerControlReportOverview(PARTNER_ID, 37L, 5)} returns controlOverview

        val auditSlot = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(auditSlot)) } returns Unit

        interactor.startControl(PARTNER_ID, 37L)

        verify(exactly = 1) { reportPersistence.startControlOnReportById(PARTNER_ID, 37L) }

        assertThat(auditSlot.captured.auditCandidate.action).isEqualTo(AuditAction.PARTNER_REPORT_CONTROL_ONGOING)
        assertThat(auditSlot.captured.auditCandidate.project?.id).isEqualTo(PROJECT_ID.toString())
        assertThat(auditSlot.captured.auditCandidate.project?.customIdentifier).isEqualTo("FG01_654")
        assertThat(auditSlot.captured.auditCandidate.project?.name).isEqualTo("acronym")
        assertThat(auditSlot.captured.auditCandidate.entityRelatedId).isEqualTo(37L)
        assertThat(auditSlot.captured.auditCandidate.description).isEqualTo("[FG01_654] [LP1] Partner report R.4 control ongoing")
    }

    @ParameterizedTest(name = "startControl - wrong status (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Submitted"], mode = EnumSource.Mode.EXCLUDE)
    fun `startControl - wrong status`(status: ReportStatus) {
        val report = report(39L, status)
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 39L) } returns report

        assertThrows<ReportNotSubmitted> { interactor.startControl(PARTNER_ID, 39L) }

        verify(exactly = 0) { reportPersistence.startControlOnReportById(any(), any()) }
        verify(exactly = 0) { auditPublisher.publishEvent(any()) }
    }

    private fun report(id: Long, status: ReportStatus): ProjectPartnerReport {
        val report = mockk<ProjectPartnerReport>()
        every { report.id } returns id
        every { report.status } returns status
        return report
    }

}
