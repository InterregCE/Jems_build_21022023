package io.cloudflight.ems.security.service.impl

import io.cloudflight.ems.security.model.CurrentUser
import io.cloudflight.ems.security.service.SecurityService
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class SecurityServiceImpl : SecurityService {
    override val currentUser: CurrentUser?
        get() {
            if (SecurityContextHolder.getContext().authentication !is UsernamePasswordAuthenticationToken) {
                return null;
            }
            return SecurityContextHolder.getContext().authentication.principal as CurrentUser;
        }

    override fun assertAdminAccess() {
        if (!currentUser!!.isAdmin) throw AccessDeniedException("User does not have admin access")
    }
}
