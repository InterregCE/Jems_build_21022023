package io.cloudflight.jems.server.project.service.workpackage.get_work_package_output

import io.cloudflight.jems.server.project.authorization.CanReadProject
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetWorkPackageOutput(
    private val workPackagePersistence: WorkPackagePersistence
) : GetWorkPackageOutputInteractor {

    @CanReadProject
    @Transactional(readOnly = true)
    override fun getWorkPackageOutputsForWorkPackage(projectId: Long, workPackageId: Long) =
        workPackagePersistence.getWorkPackageOutputsForWorkPackage(workPackageId)
}
