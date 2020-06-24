package io.cloudflight.ems.controller

import io.cloudflight.ems.api.AccountRoleApi
import io.cloudflight.ems.api.dto.OutputAccountRole
import io.cloudflight.ems.service.AccountRoleService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountRoleController(
    val accountRoleService: AccountRoleService
) : AccountRoleApi {

    override fun list(pageable: Pageable): Page<OutputAccountRole> {
        return accountRoleService.findAll(pageable = pageable)
    }

}
