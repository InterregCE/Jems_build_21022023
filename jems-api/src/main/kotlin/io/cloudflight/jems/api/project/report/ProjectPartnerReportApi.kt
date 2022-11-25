package io.cloudflight.jems.api.project.report

import io.cloudflight.jems.api.project.dto.report.ProjectPartnerReportDTO
import io.cloudflight.jems.api.project.dto.report.ProjectPartnerReportSummaryDTO
import io.cloudflight.jems.api.project.dto.report.ReportStatusDTO
import io.cloudflight.jems.api.project.dto.report.file.ProjectReportFileDTO
import io.cloudflight.jems.api.project.dto.report.file.ProjectReportFileMetadataDTO
import io.cloudflight.jems.api.project.dto.report.file.ProjectReportFileSearchRequestDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Api("Project Partner Report")
interface ProjectPartnerReportApi {

    companion object {
        const val ENDPOINT_API_PROJECT_PARTNER_REPORT = "/api/project/report/partner"
        const val ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT = "/api/project/report/partner/control"
    }

    @ApiOperation("Returns all project partner report summaries by partner id and version")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}")
    fun getProjectPartnerReports(
        @PathVariable partnerId: Long,
        pageable: Pageable,
    ): Page<ProjectPartnerReportSummaryDTO>

    @ApiOperation("Returns project partner report detail")
    @GetMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}")
    fun getProjectPartnerReport(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
    ): ProjectPartnerReportDTO

    @ApiOperation("Creates new partner report")
    @PostMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/create/{partnerId}")
    fun createProjectPartnerReport(
        @PathVariable partnerId: Long,
    ): ProjectPartnerReportSummaryDTO

    @ApiOperation("Delete project partner report")
    @DeleteMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}")
    fun deleteProjectPartnerReport(@PathVariable partnerId: Long, @PathVariable reportId: Long)

    @ApiOperation("Submit and lock partner report")
    @PostMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/submit/{partnerId}/{reportId}")
    fun submitProjectPartnerReport(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
    ): ReportStatusDTO

    @ApiOperation("Start control on submitted partner report")
    @PostMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/startControl/{partnerId}/{reportId}")
    fun startControlOnPartnerReport(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
    ): ReportStatusDTO

    @ApiOperation("Download file from partner report")
    @GetMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/{fileId}",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun downloadReportFile(@PathVariable partnerId: Long, @PathVariable fileId: Long): ResponseEntity<ByteArrayResource>

    @ApiOperation("Delete file from partner report")
    @DeleteMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}/{fileId}")
    fun deleteReportFile(@PathVariable partnerId: Long, @PathVariable reportId: Long, @PathVariable fileId: Long)

    @ApiOperation("Update description of already uploaded file")
    @PutMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}/{fileId}/description",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateReportFileDescription(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
        @PathVariable fileId: Long,
        @RequestBody(required = false) description: String?,
    )

    @ApiOperation("Upload file to partner report")
    @PostMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadReportFile(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
        @RequestPart("file") file: MultipartFile,
    ): ProjectReportFileMetadataDTO

    @ApiOperation("List attachments")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @PostMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/attachments",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun listReportFiles(
        @PathVariable partnerId: Long,
        pageable: Pageable,
        @RequestBody searchRequest: ProjectReportFileSearchRequestDTO,
    ): Page<ProjectReportFileDTO>

    @ApiOperation("Download file from partner control report")
    @GetMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT/byPartnerId/{partnerId}/{fileId}",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun downloadControlReportFile(@PathVariable partnerId: Long, @PathVariable fileId: Long): ResponseEntity<ByteArrayResource>

    @ApiOperation("Delete file from partner control report")
    @DeleteMapping("$ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}/{fileId}")
    fun deleteControlReportFile(@PathVariable partnerId: Long, @PathVariable reportId: Long, @PathVariable fileId: Long)

    @ApiOperation("Update description of already uploaded control report file")
    @PutMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}/{fileId}/description",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateControlReportFileDescription(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
        @PathVariable fileId: Long,
        @RequestBody(required = false) description: String?,
    )

    @ApiOperation("Upload file to partner control report")
    @PostMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT/byPartnerId/{partnerId}/byReportId/{reportId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadControlReportFile(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
        @RequestPart("file") file: MultipartFile,
    ): ProjectReportFileMetadataDTO

    @ApiOperation("List attachments of control report")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping("$ENDPOINT_API_PROJECT_PARTNER_CONTROL_REPORT/byPartnerIdAndReportId/{partnerId}/{reportId}/attachments")
    fun listControlReportFiles(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
        pageable: Pageable,
    ): Page<ProjectReportFileDTO>

}