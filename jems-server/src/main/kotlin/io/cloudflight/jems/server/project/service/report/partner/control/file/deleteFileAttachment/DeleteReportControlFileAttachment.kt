package io.cloudflight.jems.server.project.service.report.partner.control.file.deleteFileAttachment

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanEditPartnerControlReportFile
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.control.file.ProjectPartnerReportControlFilePersistence
import io.cloudflight.jems.server.project.service.report.partner.file.deleteProjectPartnerReportFile.FileNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteReportControlFileAttachment(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val projectPartnerReportControlFilePersistence: ProjectPartnerReportControlFilePersistence
) : DeleteReportControlFileAttachmentInteractor {

    @CanEditPartnerControlReportFile
    @Transactional()
    @ExceptionWrapper(DeleteReportControlFileAttachmentException::class)
    override fun deleteReportControlCertificateAttachment(partnerId: Long, reportId: Long, fileId: Long, attachmentId: Long) {
        val certificate = projectPartnerReportControlFilePersistence.getByReportIdAndId(reportId, fileId)
        if (certificate.signedFile == null)
            throw FileNotFound()

        if (reportPersistence.getPartnerReportById(partnerId, reportId).status !== ReportStatus.InControl)
            throw DeletionNotAllowed()

        projectPartnerReportControlFilePersistence.deleteCertificateAttachment(fileId)
    }
}
