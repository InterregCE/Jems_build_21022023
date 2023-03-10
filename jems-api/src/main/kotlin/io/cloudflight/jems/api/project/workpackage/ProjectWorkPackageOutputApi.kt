package io.cloudflight.jems.api.project.workpackage

import io.cloudflight.jems.api.project.dto.workpackage.output.WorkPackageOutputDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid
import org.springframework.web.bind.annotation.RequestParam

@Api("WorkPackageOutput")
interface ProjectWorkPackageOutputApi {

    companion object {
        private const val ENDPOINT_API_PROJECT_WORK_PACKAGE_OUTPUT =
            "/api/project/{projectId}/workPackage/{workPackageId}/output"
    }

    @ApiOperation("Returns all work package outputs for a work package")
    @GetMapping(ENDPOINT_API_PROJECT_WORK_PACKAGE_OUTPUT)
    fun getOutputs(
        @PathVariable projectId: Long,
        @PathVariable workPackageId: Long,
        @RequestParam(required = false) version: String? = null
    ): List<WorkPackageOutputDTO>

    @ApiOperation("Updates work packages outputs for a work package")
    @PutMapping(ENDPOINT_API_PROJECT_WORK_PACKAGE_OUTPUT, consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateOutputs(
        @PathVariable projectId: Long,
        @PathVariable workPackageId: Long,
        @Valid @RequestBody outputs: List<WorkPackageOutputDTO>
    ): List<WorkPackageOutputDTO>

}
