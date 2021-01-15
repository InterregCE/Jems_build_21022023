package io.cloudflight.jems.server.project.service.workpackage.output.get_work_package_output

import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput

interface GetWorkPackageOutputInteractor {

    fun getWorkPackageOutputsForWorkPackage(workPackageId: Long): List<WorkPackageOutput>

}
