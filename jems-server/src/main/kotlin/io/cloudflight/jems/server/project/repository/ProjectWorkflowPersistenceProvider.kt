package io.cloudflight.jems.server.project.repository

import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.entity.ProjectStatusHistoryEntity
import io.cloudflight.jems.server.project.service.ProjectWorkflowPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationActionInfo
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.model.ProjectStatus
import io.cloudflight.jems.server.project.service.toProjectStatus
import io.cloudflight.jems.server.user.repository.user.UserRepository
import java.time.LocalDate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProjectWorkflowPersistenceProvider(
    private val projectStatusHistoryRepository: ProjectStatusHistoryRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
) : ProjectWorkflowPersistence {

    @Transactional(readOnly = true)
    override fun getProjectEligibilityDecisionDate(projectId: Long): LocalDate? =
        getProjectOrThrow(projectId)!!.let {
            if (it.step2Active)
                it.secondStepDecision!!.eligibilityDecision?.decisionDate
            else
                it.firstStepDecision!!.eligibilityDecision?.decisionDate
        }

    @Transactional(readOnly = true)
    override fun getApplicationPreviousStatus(projectId: Long): ProjectStatus =
        getPreviousHistoryStatusOrThrow(projectId).toProjectStatus()

    @Transactional(readOnly = true)
    override fun getLatestApplicationStatusNotEqualTo(
        projectId: Long, statusToIgnore: ApplicationStatus
    ): ApplicationStatus =
        projectStatusHistoryRepository.findFirstByProjectIdAndStatusNotOrderByUpdatedDesc(
            projectId, statusToIgnore
        )?.status ?: throw ApplicationStatusNotFoundException()

    @Transactional
    override fun updateApplicationFirstSubmission(projectId: Long, userId: Long, status: ApplicationStatus) =
        projectRepository.getOne(projectId).apply {
            val newStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this, status = status, user = userRepository.getOne(userId)
                )
            )
            firstSubmission = newStatus
            currentStatus = newStatus
        }.currentStatus.status

    @Transactional
    override fun updateProjectLastResubmission(projectId: Long, userId: Long, status: ProjectStatus) =
        projectRepository.getOne(projectId).apply {
            val newStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this,
                    status = status.status,
                    user = userRepository.getOne(userId),
                    decisionDate = status.decisionDate,
                    note = status.note,
                )
            )
            lastResubmission = newStatus
            currentStatus = newStatus
        }.currentStatus.status

    @Transactional
    override fun updateProjectCurrentStatus(
        projectId: Long,
        userId: Long,
        status: ApplicationStatus,
        actionInfo: ApplicationActionInfo?
    ) =
        projectRepository.getOne(projectId).apply {
            currentStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this, status = status, user = userRepository.getOne(userId),
                    decisionDate = actionInfo?.date,
                    note = actionInfo?.note
                )
            )
        }.currentStatus.status

    @Transactional
    override fun startSecondStep(
        projectId: Long,
        userId: Long,
        actionInfo: ApplicationActionInfo?
    ): ApplicationStatus =
        projectRepository.getOne(projectId).apply {
            currentStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this, status = ApplicationStatus.DRAFT, user = userRepository.getOne(userId),
                    decisionDate = actionInfo?.date,
                    note = actionInfo?.note
                )
            )
            step2Active = true
        }.currentStatus.status

    @Transactional
    override fun revertCurrentStatusToPreviousStatus(projectId: Long) =
        getPreviousHistoryStatusOrThrow(projectId).let { previousHistoryStatus ->
            projectRepository.getOne(projectId).apply {
                projectStatusHistoryRepository.delete(currentStatus)
                currentStatus = previousHistoryStatus
            }
        }.currentStatus.status

    @Transactional
    override fun resetProjectFundingDecisionToCurrentStatus(projectId: Long) =
        projectRepository.getOne(projectId).apply {
            if (this.step2Active)
                this.secondStepDecision!!.fundingDecision = currentStatus
            else
                this.firstStepDecision!!.fundingDecision = currentStatus
        }.currentStatus.status


    @Transactional
    override fun updateProjectEligibilityDecision(
        projectId: Long, userId: Long, status: ApplicationStatus, actionInfo: ApplicationActionInfo
    ) =
        projectRepository.getOne(projectId).apply {
            val newStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this, status = status, note = actionInfo.note,
                    decisionDate = actionInfo.date, user = userRepository.getOne(userId)
                )
            )
            if (this.step2Active)
                this.secondStepDecision!!.eligibilityDecision = newStatus
            else
                this.firstStepDecision!!.eligibilityDecision = newStatus
            currentStatus = newStatus
        }.currentStatus.status

    @Transactional
    override fun clearProjectEligibilityDecision(projectId: Long) {
        projectRepository.getOne(projectId).apply {
            if (this.step2Active)
                this.secondStepDecision!!.eligibilityDecision = null
            else
                this.firstStepDecision!!.eligibilityDecision = null
        }
    }

    @Transactional
    override fun updateProjectFundingDecision(
        projectId: Long, userId: Long, status: ApplicationStatus, actionInfo: ApplicationActionInfo
    ) =
        projectRepository.getOne(projectId).apply {
            val newStatus = projectStatusHistoryRepository.save(
                ProjectStatusHistoryEntity(
                    project = this, status = status, note = actionInfo.note,
                    decisionDate = actionInfo.date, user = userRepository.getOne(userId)
                )
            )
            if (this.step2Active)
                this.secondStepDecision!!.fundingDecision = newStatus
            else
                this.firstStepDecision!!.fundingDecision = newStatus
            currentStatus = newStatus
        }.currentStatus.status

    @Transactional
    override fun clearProjectFundingDecision(projectId: Long) {
        projectRepository.getOne(projectId).apply {
            if (this.step2Active)
                this.secondStepDecision!!.fundingDecision = null
            else
                this.firstStepDecision!!.fundingDecision = null
        }
    }

    private fun getProjectOrThrow(projectId: Long) =
        projectRepository.findById(projectId).orElseThrow { ResourceNotFoundException("project") }

    private fun getPreviousHistoryStatusOrThrow(projectId: Long): ProjectStatusHistoryEntity =
        projectStatusHistoryRepository.findTop2ByProjectIdOrderByUpdatedDesc(projectId)
            .run {
                if (size != 2)
                    throw PreviousApplicationStatusNotFoundException()
                last()
            }
}