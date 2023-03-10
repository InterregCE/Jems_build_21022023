package io.cloudflight.jems.server.project.service.checklist.update

import io.cloudflight.jems.server.project.service.checklist.model.ChecklistInstance
import io.cloudflight.jems.server.project.service.checklist.model.ChecklistInstanceDetail
import io.cloudflight.jems.server.project.service.checklist.model.ChecklistInstanceStatus

interface UpdateChecklistInstanceInteractor {
    fun update(checklist: ChecklistInstanceDetail): ChecklistInstanceDetail

    fun changeStatus(checklistId: Long, status: ChecklistInstanceStatus): ChecklistInstance

    fun updateSelection(selection: Map<Long, Boolean>)

    fun updateDescription(checklistId: Long, description: String?): ChecklistInstance
}
