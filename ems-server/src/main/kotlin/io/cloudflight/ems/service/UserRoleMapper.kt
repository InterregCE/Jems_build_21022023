package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.user.OutputUserRole
import io.cloudflight.ems.entity.AccountRole

fun AccountRole.toOutputUserRole() = OutputUserRole(
    id = this.id,
    name = this.name
)


