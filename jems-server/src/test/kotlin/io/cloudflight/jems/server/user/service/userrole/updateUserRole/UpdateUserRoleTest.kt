package io.cloudflight.jems.server.user.service.userrole.updateUserRole

import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.user.service.UserRolePersistence
import io.cloudflight.jems.server.user.service.model.UserRole
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import io.cloudflight.jems.server.user.service.model.UserRoleSummary
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class UpdateUserRoleTest {

    companion object {
        private const val ROLE_ID = 2L

        private val userRoleUpdate = UserRole(
            id = ROLE_ID,
            name = "maintainer",
            permissions = setOf(UserRolePermission.ProjectSubmission)
        )

        private val userRoleSummary = UserRoleSummary(
            id = ROLE_ID,
            name = "maintainer"
        )
    }

    @MockK
    lateinit var persistence: UserRolePersistence

    @RelaxedMockK
    lateinit var generalValidator: GeneralValidatorService

    @RelaxedMockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @InjectMockKs
    lateinit var updateUserRole: UpdateUserRole

    @Test
    fun `updateUserRole - change to different role - OK`() {
        every { persistence.findById(ROLE_ID) } returns userRoleSummary
        every { persistence.findUserRoleByName(userRoleUpdate.name) } returns Optional.empty()
        every { persistence.update(any()) } returnsArgument 0

        assertThat(updateUserRole.updateUserRole(userRoleUpdate)).isEqualTo(userRoleUpdate)
        verify(exactly = 1) { persistence.update(userRoleUpdate) }
        verify(exactly = 1) { auditPublisher.publishEvent(any()) }
    }

    @Test
    fun `updateUserRole - no change in role - OK`() {
        every { persistence.findById(ROLE_ID) } returns userRoleSummary
        every { persistence.findUserRoleByName(userRoleUpdate.name) } returns Optional.of(
            UserRoleSummary(
                id = ROLE_ID,
                name = userRoleUpdate.name
            )
        )
        every { persistence.update(any()) } returnsArgument 0

        assertThat(updateUserRole.updateUserRole(userRoleUpdate)).isEqualTo(userRoleUpdate)
        verify(exactly = 1) { persistence.update(userRoleUpdate) }
    }

    @Test
    fun `updateUserRole - invalid permission setup`() {
        val userRoleUpdateInvalid = userRoleUpdate.copy(permissions = setOf(
            UserRolePermission.ProjectSubmission,
            // invalid ones:
            UserRolePermission.ProjectAssessmentChecklistConsolidate /* this one requires ProjectAssessmentChecklistUpdate */,
        ))

        every { persistence.findById(ROLE_ID) } returns userRoleSummary
        every { persistence.findUserRoleByName(userRoleUpdateInvalid.name) } returns Optional.empty()

        assertThrows<UserRolePermissionCombinationInvalid> { updateUserRole.updateUserRole(userRoleUpdateInvalid) }
    }

    @Test
    fun `updateUserRole - role name already taken`() {
        every { persistence.findById(ROLE_ID) } returns userRoleSummary
        every { persistence.findUserRoleByName(userRoleUpdate.name) } returns Optional.of(
            UserRoleSummary(
                id = 126L,
                name = userRoleUpdate.name
            )
        )
        assertThrows<UserRoleNameAlreadyTaken> { updateUserRole.updateUserRole(userRoleUpdate) }
    }

}
