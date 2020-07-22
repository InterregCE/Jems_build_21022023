package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.user.OutputUserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRoleService {

    fun findAll(pageable: Pageable): Page<OutputUserRole>

}