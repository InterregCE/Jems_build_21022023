package io.cloudflight.jems.server.service

import io.cloudflight.jems.api.user.dto.InputUserProfile
import io.cloudflight.jems.server.user.entity.UserEntity
import io.cloudflight.jems.server.user.entity.UserProfile
import io.cloudflight.jems.server.user.entity.UserRoleEntity
import io.cloudflight.jems.server.user.repository.UserProfileRepository
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.user.service.UserProfileService
import io.cloudflight.jems.server.user.service.UserProfileServiceImpl
import io.cloudflight.jems.server.user.service.toOutputUserProfile
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class UserProfileServiceTest {

    @MockK
    lateinit var userProfileRepository: UserProfileRepository

    @MockK
    lateinit var securityService: SecurityService

    lateinit var userProfileService: UserProfileService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        userProfileService = UserProfileServiceImpl(userProfileRepository, securityService)
    }

    @Test
    fun getUserProfileExistent() {
        val expectedProfile = UserProfile(
            id = 85,
            language = "en"
        )
        every { securityService.currentUser!!.user.id } returns 1
        every { userProfileRepository.findByIdOrNull(1) } returns expectedProfile

        val result = userProfileService.getUserProfile()

        Assertions.assertThat(result).isEqualTo(expectedProfile.toOutputUserProfile())
    }

    @Test
    fun getUserProfileInexistent() {
        every { securityService.currentUser!!.user.id } returns 1
        every { userProfileRepository.findByIdOrNull(1) } returns null

        val result = userProfileService.getUserProfile()

        Assertions.assertThat(result).isEqualTo(null)
    }

    @Test
    fun setUserProfileExistent() {
        val profileToSave = UserProfile(
            id = 1,
            language = "de"
        )
        every { userProfileRepository.save(profileToSave) } returns profileToSave
        every { securityService.currentUser!!.user.id } returns 1

        val result = userProfileService.setUserProfile(InputUserProfile("de"))

        val expectedProfile = UserProfile(
            id = 1,
            language = "de"
        ).toOutputUserProfile()

        Assertions.assertThat(result).isEqualTo(expectedProfile)
    }

    @Test
    fun setUserProfileInexistent() {
        val profileToSave = UserProfile(
            id = 1,
            language = "de"
        )
        every { userProfileRepository.save(profileToSave) } returns profileToSave
        every { securityService.currentUser!!.user.id } returns 1

        val result = userProfileService.setUserProfile(InputUserProfile("de"))

        val expectedProfile = UserProfile(
            id = 1,
            language = "de"
        ).toOutputUserProfile()

        Assertions.assertThat(result).isEqualTo(expectedProfile)
    }
}
