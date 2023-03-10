package io.cloudflight.jems.server.project.service.report.partner.control.file

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.partner.report.PartnerControlReportExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.UserSummaryData
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.authentication.model.LocalCurrentUser
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.plugin.JemsPluginRegistry
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileCreate
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileType
import io.cloudflight.jems.server.project.service.report.model.partner.PartnerReportIdentification
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.control.file.generateExport.GenerateReportControlExport
import io.cloudflight.jems.server.project.service.report.partner.control.file.generateExport.GenerateReportControlExportException
import io.cloudflight.jems.server.resources.service.get_logos.GetLogosInteractor
import io.cloudflight.jems.server.user.service.model.User
import io.cloudflight.jems.server.user.service.model.UserRole
import io.cloudflight.jems.server.user.service.model.UserStatus
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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.IOException
import java.time.ZonedDateTime

class GenerateReportControlExportTest : UnitTest() {


    companion object {
        const val pluginKey = "standard-partner-control-report-export-plugin"
        const val PARTNER_ID = 1L
        const val PROJECT_ID = 1L
        const val REPORT_ID = 3L
        private val YESTERDAY = ZonedDateTime.now().minusDays(1)

        val CONTROLER_ROLE = UserRole(id = 7, name = "controller", permissions = emptySet(), isDefault = false)
        val userController = User(
            id = 3,
            email = "controller@jems.eu",
            name = "John",
            surname = "Doe",
            userRole = CONTROLER_ROLE,
            userStatus = UserStatus.ACTIVE
        )
        val localControllerUser = LocalCurrentUser(
            userController, "hash_pass", listOf(
                SimpleGrantedAuthority("ROLE_" + CONTROLER_ROLE.name)
            )
        )


        val partnerReport = ProjectPartnerReport(
            id = REPORT_ID,
            reportNumber = 3,
            status = ReportStatus.Certified,
            version = "1",
            firstSubmission = YESTERDAY,
            identification = PartnerReportIdentification(
                projectIdentifier = "CLF00001",
                projectAcronym = "acronym",
                partnerNumber = 1,
                partnerAbbreviation = "CBG",
                partnerRole = ProjectPartnerRole.LEAD_PARTNER,
                coFinancing = listOf(),
                nameInEnglish = "Costa-Bianco Group",
                nameInOriginalLanguage = "Costa-Bianco Group",
                legalStatus = null,
                partnerType = null,
                vatRecovery = null,
                country = null,
                currency = null
            )
        )
    }

    @MockK
    lateinit var jemsPluginRegistry: JemsPluginRegistry

    @MockK
    lateinit var getLogosInteractor: GetLogosInteractor

    @MockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @MockK
    lateinit var reportPersistence: ProjectPartnerReportPersistence

    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @MockK
    lateinit var partnerControlReportExportPlugin: PartnerControlReportExportPlugin

    @MockK
    lateinit var projectPartnerReportControlFilePersistence: ProjectPartnerReportControlFilePersistence

    @MockK
    lateinit var securityService: SecurityService

    @InjectMockKs
    lateinit var generateReportControlExport: GenerateReportControlExport


    @BeforeEach
    fun setup() {

        every { securityService.currentUser } returns localControllerUser
        every { securityService.getUserIdOrThrow() } returns localControllerUser.user.id
        clearMocks(auditPublisher)
    }

    @Test
    fun `Should generate the control report export`() {
        val exportResult = ExportResult("pdf", "Control Report - CLF00001 - LP1 - R3.pdf", byteArrayOf())
        val currentUserSummaryData = UserSummaryData(
            id = userController.id,
            email = userController.email,
            name = userController.name,
            surname = userController.surname
        )
        val identifier = 73L

        every { reportPersistence.getPartnerReportById(partnerId = PARTNER_ID, reportId = REPORT_ID) } returns partnerReport
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID, partnerReport.version) } returns PROJECT_ID
        every { jemsPluginRegistry.get(PartnerControlReportExportPlugin::class, pluginKey) } returns partnerControlReportExportPlugin
        every { projectPartnerReportControlFilePersistence.countReportControlFilesByFileType(REPORT_ID, JemsFileType.ControlReport) } returns identifier - 1
        every {
            getLogosInteractor.getLogos()
        } returns listOf()

        every {
            partnerControlReportExportPlugin.export(
                PROJECT_ID,
                PARTNER_ID,
                REPORT_ID,
                logo = null,
                creationDate = any(),
                currentUser = currentUserSummaryData
            )
        } returns exportResult

        val slotAudit = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(slotAudit)) } returns mockk()


        val slot = slot<JemsFileCreate>()
        every { projectPartnerReportControlFilePersistence.saveReportControlFile(REPORT_ID, capture(slot)) } returns mockk()

        generateReportControlExport.export(PARTNER_ID, REPORT_ID, pluginKey)

        verify(exactly = 1) { projectPartnerReportControlFilePersistence.saveReportControlFile(REPORT_ID, slot.captured) }
        verify(exactly = 1) { auditPublisher.publishEvent(capture(slotAudit)) }
        assertThat(slotAudit.captured.auditCandidate.action).isEqualTo(AuditAction.CONTROL_REPORT_EXPORT_GENERATED)
        assertThat(slotAudit.captured.auditCandidate.description).isEqualTo("A control report was generated for partner report R.3 of partner LP1")

        assertThat(slot.captured.type).isEqualTo(JemsFileType.ControlReport)
        assertThat(slot.captured.name).matches("Control Report $identifier - CLF00001 - LP1 - R3.pdf")
    }

    @Test
    fun `throws generate certificate exception`() {
        every { reportPersistence.getPartnerReportById(partnerId = PARTNER_ID, reportId = REPORT_ID) } returns partnerReport
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID, partnerReport.version) } returns PROJECT_ID
        val exception = GenerateReportControlExportException(IOException())
        every { jemsPluginRegistry.get(PartnerControlReportExportPlugin::class, pluginKey) } throws exception

        assertThrows<GenerateReportControlExportException> {
            generateReportControlExport.export(PARTNER_ID, REPORT_ID, pluginKey)
        }

    }

}
