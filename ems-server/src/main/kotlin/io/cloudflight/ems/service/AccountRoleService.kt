package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.OutputUserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AccountRoleService {

    fun findAll(pageable: Pageable): Page<OutputUserRole>

}