package io.cloudflight.jems.api.project.report

import io.cloudflight.jems.api.project.dto.report.ProjectPartnerReportDTO
import io.cloudflight.jems.api.project.dto.report.ProjectPartnerReportSummaryDTO
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

@Api("Project Partner Report")
interface ProjectPartnerReportApi {

    companion object {
        const val ENDPOINT_API_PROJECT_PARTNER_REPORT = "/api/project/report/partner"
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

    @ApiOperation("Submit and lock partner report")
    @PostMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/submit/{partnerId}/{reportId}")
    fun submitProjectPartnerReport(
        @PathVariable partnerId: Long,
        @PathVariable reportId: Long,
    ): ProjectPartnerReportSummaryDTO

    @ApiOperation("Download file from partner report")
    @GetMapping(
        "$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/{fileId}",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun downloadAttachment(@PathVariable partnerId: Long, @PathVariable fileId: Long): ResponseEntity<ByteArrayResource>

    @ApiOperation("Delete file from partner report")
    @DeleteMapping("$ENDPOINT_API_PROJECT_PARTNER_REPORT/byPartnerId/{partnerId}/{fileId}")
    fun deleteAttachment(@PathVariable partnerId: Long, @PathVariable fileId: Long)

}
