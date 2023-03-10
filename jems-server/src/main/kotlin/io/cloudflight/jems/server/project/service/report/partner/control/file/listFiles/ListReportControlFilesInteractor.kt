package io.cloudflight.jems.server.project.service.report.partner.control.file.listFiles

import io.cloudflight.jems.server.project.service.report.model.partner.control.file.PartnerReportControlFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ListReportControlFilesInteractor {
    fun list(partnerId: Long, reportId: Long, pageable: Pageable): Page<PartnerReportControlFile>
}
