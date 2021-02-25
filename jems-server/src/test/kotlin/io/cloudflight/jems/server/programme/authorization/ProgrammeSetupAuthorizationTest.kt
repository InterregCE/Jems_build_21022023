package io.cloudflight.jems.server.programme.authorization

import io.cloudflight.jems.server.authentication.model.LocalCurrentUser
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.adminUser
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.applicantUser
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.programmeUser
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProgrammeSetupAuthorizationTest {

    @MockK
    lateinit var securityService: SecurityService

    lateinit var programmeSetupAuthorization: ProgrammeSetupAuthorization

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        programmeSetupAuthorization =
            ProgrammeSetupAuthorization(securityService)
    }

    @ParameterizedTest
    @MethodSource("provideAdminAndProgramUsers")
    fun `this user can access setup`(currentUser: LocalCurrentUser) {
        every { securityService.currentUser } returns currentUser

        assertTrue(
            programmeSetupAuthorization.canAccessSetup(),
            "${currentUser.user.email} should be able to access programme setup"
        )
    }

    @Test
    fun `applicant user cannot access setup`() {
        every { securityService.currentUser } returns applicantUser

        assertFalse(
            programmeSetupAuthorization.canAccessSetup(),
            "${applicantUser.user.email} should not be able to access programme setup"
        )
    }

    @ParameterizedTest
    @MethodSource("provideAllUsers")
    fun `this user can read indicators`(currentUser: LocalCurrentUser) {
        every { securityService.currentUser } returns currentUser

        assertTrue(
            programmeSetupAuthorization.canReadProgrammeSetup(),
            "${currentUser.user.email} should be able to read indicators"
        )
    }

    @ParameterizedTest
    @MethodSource("provideAllUsers")
    fun `this user can read programme nuts`(currentUser: LocalCurrentUser) {
        every { securityService.currentUser } returns currentUser

        assertTrue(
            programmeSetupAuthorization.canReadNuts(),
            "${currentUser.user.email} should be able to read programme nuts"
        )
    }

    private fun provideAdminAndProgramUsers(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(programmeUser),
            Arguments.of(adminUser)
        )
    }

    private fun provideAllUsers(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(applicantUser),
            Arguments.of(programmeUser),
            Arguments.of(adminUser),
        )
    }

}
