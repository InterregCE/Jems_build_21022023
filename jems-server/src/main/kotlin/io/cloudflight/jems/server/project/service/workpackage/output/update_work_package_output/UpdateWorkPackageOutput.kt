package io.cloudflight.jems.server.project.service.workpackage.output.update_work_package_output

import io.cloudflight.jems.server.project.authorization.CanUpdateProjectWorkPackage
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput
import io.cloudflight.jems.server.project.service.workpackage.output.validateWorkPackageOutputsLimit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateWorkPackageOutput(
    private val workPackagePersistence: WorkPackagePersistence
) : UpdateWorkPackageOutputInteractor {

    @CanUpdateProjectWorkPackage
    @Transactional
    override fun updateOutputsForWorkPackage(
        workPackageId: Long,
        outputs: List<WorkPackageOutput>,
    ): List<WorkPackageOutput> {
        validateWorkPackageOutputsLimit(outputs)
        return workPackagePersistence.updateWorkPackageOutputs(workPackageId, outputs)
    }
}
