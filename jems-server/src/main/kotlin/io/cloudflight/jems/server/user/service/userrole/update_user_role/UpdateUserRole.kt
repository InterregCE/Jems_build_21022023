package io.cloudflight.jems.server.user.service.userrole.update_user_role

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.user.service.UserRolePersistence
import io.cloudflight.jems.server.user.service.authorization.CanUpdateRole
import io.cloudflight.jems.server.user.service.model.UserRole
import io.cloudflight.jems.server.user.service.userrole.userRoleUpdated
import io.cloudflight.jems.server.user.service.userrole.validateUserRoleCommon
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateUserRole(
    private val persistence: UserRolePersistence,
    private val generalValidator: GeneralValidatorService,
    private val auditPublisher: ApplicationEventPublisher
) : UpdateUserRoleInteractor {

    @CanUpdateRole
    @Transactional
    @ExceptionWrapper(UpdateUserRoleException::class)
    override fun updateUserRole(userRole: UserRole): UserRole {
        validateUserRoleCommon(generalValidator, userRole.name)
        val existingRole = persistence.findById(userRole.id).orElseThrow { UserRoleNotFound() }
        validateUserRoleNameNotTaken(userRole)

        return persistence.update(userRole).also {
            auditPublisher.publishEvent(userRoleUpdated(this, it, existingRole.name))
        }
    }

    private fun validateUserRoleNameNotTaken(userRole: UserRole) {
        val roleWithSameName = persistence.findUserRoleByName(userRole.name)
        if (roleWithSameName.isPresent && roleWithSameName.get().id != userRole.id)
            throw UserRoleNameAlreadyTaken()
    }

}
