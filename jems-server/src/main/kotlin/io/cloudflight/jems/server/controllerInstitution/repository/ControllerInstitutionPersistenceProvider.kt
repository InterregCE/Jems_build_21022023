package io.cloudflight.jems.server.controllerInstitution.repository

import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.controllerInstitution.service.getControllerInstitution.GetControllerInstitutionException
import io.cloudflight.jems.server.controllerInstitution.service.model.ControllerInstitutionList
import io.cloudflight.jems.server.controllerInstitution.service.model.ControllerInstitution
import io.cloudflight.jems.server.controllerInstitution.service.model.UpdateControllerInstitution
import io.cloudflight.jems.server.controllerInstitution.service.model.ControllerInstitutionUser
import io.cloudflight.jems.server.controllerInstitution.service.model.InstitutionPartnerDetails
import io.cloudflight.jems.server.controllerInstitution.service.model.InstitutionPartnerAssignment
import io.cloudflight.jems.server.controllerInstitution.service.model.InstitutionPartnerAssignmentWithUsers
import io.cloudflight.jems.server.controllerInstitution.service.model.UserInstitutionAccessLevel
import io.cloudflight.jems.server.nuts.repository.NutsRegion3Repository
import io.cloudflight.jems.server.project.entity.partner.ControllerInstitutionEntity
import io.cloudflight.jems.server.project.service.report.model.file.UserSimple
import io.cloudflight.jems.server.user.repository.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Repository
class ControllerInstitutionPersistenceProvider(
    private val nutsRegion3Repository: NutsRegion3Repository,
    private val institutionRepository: ControllerInstitutionRepository,
    private val institutionUserRepository: ControllerInstitutionUserRepository,
    private val institutionPartnerRepository: ControllerInstitutionPartnerRepository,
    private val userRepository: UserRepository,
): ControllerInstitutionPersistence {

    @Transactional(readOnly = true)
    override fun getControllerInstitutions(pageable: Pageable): Page<ControllerInstitutionList> =
        institutionRepository.findAll(pageable).toListModel()

    @Transactional(readOnly = true)
    override fun getControllerInstitutions(partnerIds: Set<Long>): Map<Long, ControllerInstitutionList> =
        institutionPartnerRepository.findAllByPartnerIdIn(partnerIds)
            .associate { Pair(it.partnerId, it.institution.toListModel()) }

    @Transactional(readOnly = true)
    override fun getAllControllerInstitutions(): List<ControllerInstitutionEntity> {
        return institutionRepository.findAll()
    }

    @Transactional(readOnly = true)
    override fun getControllerInstitutionsByUserId(userId: Long, pageable: Pageable): Page<ControllerInstitutionList> {
       val allowedControllerInstitutionIds = institutionUserRepository.findAllByUserId(userId).map { it.id.controllerInstitutionId }
       return institutionRepository.findAllByIdIn(allowedControllerInstitutionIds, pageable).toListModel()
    }

    @Transactional(readOnly = true)
    override fun getControllerInstitutionById(controllerInstitutionId: Long): ControllerInstitution {
        val getUsersAssignedToInstitution = institutionUserRepository.findAllByControllerInstitutionId(controllerInstitutionId)
        return this.getControllerInstitutionOrThrow(controllerInstitutionId).toModel(getUsersAssignedToInstitution)
    }


    @Transactional
    override fun createControllerInstitution(controllerInstitution: UpdateControllerInstitution): ControllerInstitution {
        val savedControllerInstitution = institutionRepository.save(controllerInstitution.toEntity())
        if (controllerInstitution.institutionNuts.isNotEmpty()) {
            savedControllerInstitution.institutionNuts = nutsRegion3Repository.findAllById(controllerInstitution.institutionNuts)
                .toMutableSet()
        }
        return savedControllerInstitution.toModel()
    }

    @Transactional
    override fun updateControllerInstitution(controllerInstitution: UpdateControllerInstitution): ControllerInstitution {
        val controllerInstitutionInstance = this.getControllerInstitutionOrThrow(controllerInstitution.id)

        controllerInstitutionInstance.description = controllerInstitution.description
        controllerInstitutionInstance.name = controllerInstitution.name
        controllerInstitutionInstance.institutionNuts = nutsRegion3Repository.findAllById(controllerInstitution.institutionNuts)
            .toMutableSet()
        return controllerInstitutionInstance.toModel()
    }

    @Transactional
    override fun updateControllerInstitutionUsers(
        institutionId: Long,
        usersToUpdate: Set<ControllerInstitutionUser>,
        usersIdsToDelete: Set<Long>
    ): Set<ControllerInstitutionUser> {
        if (usersIdsToDelete.isNotEmpty()) {
            institutionUserRepository.deleteAllByIdControllerInstitutionIdAndIdUserIdIn(institutionId, usersIdsToDelete)
        }
        val users = userRepository.findAllByEmailInIgnoreCaseOrderByEmail(usersToUpdate.map { it.userEmail }).associateBy { it.email }
        institutionUserRepository.saveAll(usersToUpdate.map { userToSave ->
            userToSave.toEntity(
                institutionId,
                users[userToSave.userEmail]!!
            )
        })
       return institutionUserRepository.findAllByControllerInstitutionId(institutionId).map { it.toModel() }.toSet()
    }

    @Transactional(readOnly = true)
    override fun getInstitutionUserByInstitutionIdAndUserId(institutionId: Long, userId: Long): Optional<ControllerInstitutionUser> =
        institutionUserRepository.findByInstitutionIdAndUserId(institutionId = institutionId, userId = userId).map { it.toModel() }


    @Transactional(readOnly = true)
    override fun getInstitutionUsersByInstitutionId(institutionId: Long): List<ControllerInstitutionUser> {
       return institutionUserRepository.findAllByControllerInstitutionId(institutionId).map { it.toModel() }
    }

    @Transactional(readOnly = true)
    override fun getControllerUsersForReportByInstitutionId(institutionId: Long): List<UserSimple> {
        return institutionUserRepository.findAllByControllerInstitutionId(institutionId).map { it.toModelForReport() }
    }

    @Transactional(readOnly = true)
    override fun getControllerInstitutionUsersByInstitutionIds(institutionIds: Set<Long>): List<ControllerInstitutionUser>  =
        institutionUserRepository.findAllByControllerInstitutionIdIn(institutionIds).map { it.toModel() }


    @Transactional(readOnly = true)
    override fun getInstitutionPartnerAssignments(pageable: Pageable): Page<InstitutionPartnerDetails> {
        return institutionPartnerRepository.getInstitutionPartnerAssignments(pageable).toModel()
    }

    @Transactional
    override fun assignInstitutionToPartner(
        partnerIdsToRemove: Set<Long>,
        assignmentsToSave: List<InstitutionPartnerAssignment>
    ): List<InstitutionPartnerAssignment> {
        if (partnerIdsToRemove.isNotEmpty()) {
            institutionPartnerRepository.deleteAllByIdInBatch(partnerIdsToRemove)
        }
        val institutions = institutionRepository.findAllById(assignmentsToSave.mapTo(HashSet()) { it.institutionId })
            .associateBy { it.id }
        return institutionPartnerRepository.saveAll(
            assignmentsToSave.toEntities(institutionResolver = { institutions[it]!! })
        ).toModels()
    }

    @Transactional
    override fun getInstitutionPartnerAssignmentsByInstitutionId(institutionId: Long): List<InstitutionPartnerAssignment> =
        institutionPartnerRepository.findAllByInstitutionId(institutionId).toModels()


    @Transactional(readOnly = true)
    override fun getInstitutionPartnerAssignmentsWithUsersByPartnerProjectIdsIn(partnerProjectIds: Set<Long>): List<InstitutionPartnerAssignmentWithUsers> =
        institutionPartnerRepository.getInstitutionPartnerAssignmentsWithUsersByPartnerProjectIdsIn(partnerProjectIds)

    @Transactional(readOnly = true)
    override fun getRelatedUserIdsForProject(projectId: Long) =
        institutionPartnerRepository.getRelatedUserIdsForProject(projectId = projectId)

    @Transactional(readOnly = true)
    override fun getControllerUserAccessLevelForPartner(userId: Long, partnerId: Long): UserInstitutionAccessLevel? =
         institutionPartnerRepository.getControllerUserAccessLevelForPartner(userId, partnerId)

    @Transactional(readOnly = true)
    override fun getRelatedProjectAndPartnerIdsForUser(userId: Long) =
        institutionPartnerRepository.getRelatedProjectIdsForUser(userId = userId)
            .groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }

    private fun getControllerInstitutionOrThrow(id: Long): ControllerInstitutionEntity =
        institutionRepository.findById(id).orElseThrow { GetControllerInstitutionException() }

    @Transactional(readOnly = true)
    override fun getInstitutionPartnerAssignmentsToDeleteByProjectId(projectId: Long): List<InstitutionPartnerAssignment> =
        institutionPartnerRepository.getInstitutionPartnerAssignmentsToDeleteByProjectId(projectId).toModels()

    @Transactional(readOnly = true)
    override fun getInstitutionPartnerAssignmentsToDeleteByInstitutionId(institutionId: Long): List<InstitutionPartnerAssignment> =
        institutionPartnerRepository.getInstitutionPartnerAssignmentsToDeleteByInstitutionId(institutionId).toModels()


}
