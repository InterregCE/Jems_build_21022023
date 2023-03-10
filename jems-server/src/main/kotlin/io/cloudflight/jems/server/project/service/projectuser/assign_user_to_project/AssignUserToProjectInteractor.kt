package io.cloudflight.jems.server.project.service.projectuser.assign_user_to_project

import io.cloudflight.jems.server.user.service.model.assignment.UpdateProjectUser

interface AssignUserToProjectInteractor {
    fun updateUserAssignmentsOnProject(data: Set<UpdateProjectUser>)
}
