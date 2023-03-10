package io.cloudflight.jems.api.project.workpackage;

import io.cloudflight.jems.api.project.dto.workpackage.activity.WorkPackageActivityDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Api("WorkPackageActivity")
interface ProjectWorkPackageActivityApi {

    companion object {
        private const val ENDPOINT_API_PROJECT_WORK_PACKAGE_ACTIVITY =
            "/api/project/{projectId}/workPackage/{workPackageId}/activity"
    }

    @ApiOperation("Returns all work package activities for a work package")
    @GetMapping(ENDPOINT_API_PROJECT_WORK_PACKAGE_ACTIVITY)
    fun getActivities(
        @PathVariable projectId: Long,
        @PathVariable workPackageId: Long,
        @RequestParam(required = false) version: String? = null
    ): List<WorkPackageActivityDTO>

    @ApiOperation("Updates activities for a work package")
    @PutMapping(ENDPOINT_API_PROJECT_WORK_PACKAGE_ACTIVITY, consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateActivities(
        @PathVariable projectId: Long,
        @PathVariable workPackageId: Long,
        @RequestBody activities: List<WorkPackageActivityDTO>
    ): List<WorkPackageActivityDTO>

}
