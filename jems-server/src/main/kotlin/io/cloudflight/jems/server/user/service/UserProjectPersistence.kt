package io.cloudflight.jems.server.user.service

interface UserProjectPersistence {

    fun getProjectIdsForUser(userId: Long): Set<Long>

    fun getUserIdsForProject(projectId: Long): Set<Long>

    fun changeUsersAssignedToProject(projectId: Long, userIdsToRemove: Set<Long>, userIdsToAssign: Set<Long>): Set<Long>

}
