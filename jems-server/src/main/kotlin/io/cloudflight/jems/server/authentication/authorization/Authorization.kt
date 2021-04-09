package io.cloudflight.jems.server.authentication.authorization

import io.cloudflight.jems.server.authentication.model.APPLICANT_USER
import io.cloudflight.jems.server.authentication.service.SecurityService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@authorization.isAdmin()")
annotation class IsAdmin

@Component
class Authorization(
    open val securityService: SecurityService
) {

    fun isAdmin(): Boolean =
        securityService.currentUser?.isAdmin!!

    fun isProgrammeUser(): Boolean =
        securityService.currentUser?.isProgrammeUser!!

    fun isApplicantUser(): Boolean =
        securityService.currentUser?.hasRole(APPLICANT_USER)!!

    protected fun isApplicantOwner(applicantId: Long): Boolean =
        isApplicantUser() && applicantId == securityService.currentUser?.user?.id

    protected fun isApplicantNotOwner(applicantId: Long): Boolean =
        isApplicantUser() && applicantId != securityService.currentUser?.user?.id

}
