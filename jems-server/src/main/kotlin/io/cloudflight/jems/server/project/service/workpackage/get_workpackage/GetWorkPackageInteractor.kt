package io.cloudflight.jems.server.project.service.workpackage.get_workpackage

import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackage
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackageSimple
import io.cloudflight.jems.server.project.service.workpackage.model.ProjectWorkPackage

interface GetWorkPackageInteractor {
    fun getWorkPackagesForTimePlanByProjectId(projectId: Long, version: String? = null): List<ProjectWorkPackage>

    fun getWorkPackagesByProjectId(projectId: Long, version: String?): List<OutputWorkPackageSimple>

    fun getWorkPackageById(projectId: Long, workPackageId: Long, version: String?): OutputWorkPackage
}
