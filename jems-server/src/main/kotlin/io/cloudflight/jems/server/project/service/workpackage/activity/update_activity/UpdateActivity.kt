package io.cloudflight.jems.server.project.service.workpackage.activity.update_activity

import io.cloudflight.jems.server.project.authorization.CanUpdateProject
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.activity.validateWorkPackageActivities
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateActivity(
    private val persistence: WorkPackagePersistence
) : UpdateActivityInteractor {

    @CanUpdateProject
    @Transactional
    override fun updateActivitiesForWorkPackage(
        projectId: Long,
        workPackageId: Long,
        activities: List<WorkPackageActivity>
    ): List<WorkPackageActivity> {
        validateWorkPackageActivities(activities)
        return persistence.updateWorkPackageActivities(workPackageId, activities)
    }

}
