package io.cloudflight.jems.server.user.controller

import io.cloudflight.jems.api.user.UserRoleApi
import io.cloudflight.jems.api.user.dto.UserRoleCreateDTO
import io.cloudflight.jems.api.user.dto.UserRoleDTO
import io.cloudflight.jems.api.user.dto.UserRoleSummaryDTO
import io.cloudflight.jems.server.user.service.userrole.create_user_role.CreateUserRoleInteractor
import io.cloudflight.jems.server.user.service.userrole.get_user_role.GetUserRoleInteractor
import io.cloudflight.jems.server.user.service.userrole.update_user_role.UpdateUserRoleInteractor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRoleController(
    private val getUserRoleInteractor: GetUserRoleInteractor,
    private val createUserRoleInteractor: CreateUserRoleInteractor,
    private val updateUserRoleInteractor: UpdateUserRoleInteractor,
) : UserRoleApi {

    override fun list(pageable: Pageable): Page<UserRoleSummaryDTO> =
        getUserRoleInteractor.getUserRoles(pageable).toDto()

    override fun createUserRole(role: UserRoleCreateDTO): UserRoleDTO =
        createUserRoleInteractor.createUserRole(role.toCreateModel()).toDto()

    override fun updateUserRole(role: UserRoleDTO): UserRoleDTO =
        updateUserRoleInteractor.updateUserRole(role.toModel()).toDto()

    override fun getById(id: Long): UserRoleDTO =
        getUserRoleInteractor.getUserRoleById(id).toDto()

}
