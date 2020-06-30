package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.InputProject
import io.cloudflight.ems.api.dto.OutputProject
import io.cloudflight.ems.entity.Audit
import io.cloudflight.ems.exception.ResourceNotFoundException
import io.cloudflight.ems.repository.AccountRepository
import io.cloudflight.ems.repository.ProjectRepository
import io.cloudflight.ems.security.ADMINISTRATOR
import io.cloudflight.ems.security.PROGRAMME_USER
import io.cloudflight.ems.security.service.SecurityService
import io.cloudflight.ems.service.ProjectDtoUtilClass.Companion.getDtoFrom
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectServiceImpl(
    private val projectRepo: ProjectRepository,
    private val accountRepository: AccountRepository,
    private val auditService: AuditService,
    private val securityService: SecurityService
) : ProjectService {

    companion object {
        private val logger = LoggerFactory.getLogger(ProjectServiceImpl::class.java)
    }

    @Transactional(readOnly = true)
    override fun getProjects(page: Pageable): Page<OutputProject> {
        val currentUser = securityService.currentUser!!
        if (currentUser.hasRole(ADMINISTRATOR) || currentUser.hasRole(PROGRAMME_USER)) {
            return projectRepo.findAll(page).map { getDtoFrom(it) }
        } else {
            return projectRepo.findAllByApplicant_Id(currentUser.user.id!!, page).map { getDtoFrom(it) }
        }
    }

    @Transactional
    override fun createProject(project: InputProject): OutputProject {
        val applicant = accountRepository.findByIdOrNull(securityService.currentUser?.user?.id!!)
            ?: throw ResourceNotFoundException()

        val createdProject = projectRepo.save(project.toEntity(applicant))
        auditService.logEvent(Audit.projectSubmitted(securityService.currentUser, createdProject.id.toString()))

        return getDtoFrom(createdProject)
    }

    @Transactional(readOnly = true)
    override fun getProjectById(id: Long): OutputProject {
        return getDtoFrom(projectRepo.findOneById(id)
            ?: let {
                logger.error("Project with id $id was not found.")
                throw ResourceNotFoundException();
            })
    }
}
