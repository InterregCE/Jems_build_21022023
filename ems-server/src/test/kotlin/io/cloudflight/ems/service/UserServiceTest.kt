package io.cloudflight.ems.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.cloudflight.ems.api.dto.user.InputUserCreate
import io.cloudflight.ems.api.dto.user.InputUserRegistration
import io.cloudflight.ems.api.dto.user.OutputUser
import io.cloudflight.ems.api.dto.user.OutputUserRole
import io.cloudflight.ems.entity.Account
import io.cloudflight.ems.entity.AccountRole
import io.cloudflight.ems.entity.Audit
import io.cloudflight.ems.entity.AuditAction
import io.cloudflight.ems.exception.I18nFieldError
import io.cloudflight.ems.exception.I18nValidationError
import io.cloudflight.ems.exception.ResourceNotFoundException
import io.cloudflight.ems.repository.AccountRepository
import io.cloudflight.ems.repository.AccountRoleRepository
import io.cloudflight.ems.security.model.LocalCurrentUser
import io.cloudflight.ems.security.service.SecurityService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class UserServiceTest {

    private val UNPAGED = Pageable.unpaged()

    private val user = OutputUser(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        userRole = OutputUserRole(id = 1, name = "ADMIN")
    )

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var accountRoleRepository: AccountRoleRepository

    @RelaxedMockK
    lateinit var auditService: AuditService

    @RelaxedMockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var securityService: SecurityService

    lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { securityService.currentUser } returns LocalCurrentUser(user, "hash_pass", emptyList())
        userService =
            UserServiceImpl(accountRepository, accountRoleRepository, auditService, securityService, passwordEncoder)
    }

    @Test
    fun getAllUsers() {
        val userToReturn = Account(
            id = 85,
            email = "admin@ems.io",
            name = "Name",
            surname = "Surname",
            accountRole = AccountRole(9, "admin"),
            password = "hash_pass"
        )
        every { accountRepository.findAll(UNPAGED) } returns PageImpl(listOf(userToReturn))

        // test start
        val result = userService.findAll(UNPAGED)

        // assertions:
        assertThat(result.totalElements).isEqualTo(1);

        val expectedUsers = listOf(
            OutputUser(
                id = 85,
                email = "admin@ems.io",
                name = "Name",
                surname = "Surname",
                userRole = OutputUserRole(9, "admin")
            )
        )
        assertThat(result.stream()).isEqualTo(expectedUsers);
    }

    @Test
    fun getUser_empty() {
        every { accountRepository.findOneByEmail(eq("not_existing@ems.io")) } returns null

        val result = userService.findOneByEmail("not_existing@ems.io")
        assertThat(result).isNull();
    }

    @Test
    fun createUser_wrong() {
        every { accountRoleRepository.findById(any()) } returns Optional.empty()

        val account = InputUserCreate(
            email = "existing@user.com",
            name = "Ondrej",
            surname = "Tester",
            accountRoleId = 10 // does not exist
        )

        val exception = assertThrows<I18nValidationError> { userService.create(account) }
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus)

        val expectedErrors = mapOf(
            "accountRoleId" to I18nFieldError("user.accountRoleId.does.not.exist")
        )
        assertThat(exception.i18nFieldErrors).isEqualTo(expectedErrors)
    }

    @Test
    fun registerUser_missingApplicantRole() {
        every { accountRoleRepository.findOneByName(any()) } returns null

        val user = InputUserRegistration(
            email = "new@user.com",
            name = "Ondrej",
            surname = "Tester",
            password = "raw_password"
        )

        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
        logger.addAppender(listAppender)

        assertThrows<ResourceNotFoundException> { userService.registerApplicant(user) }
        Assertions.assertLinesMatch(
            listOf("The default applicant role cannot be found in the system."),
            listAppender.list.map { it.formattedMessage }
        )
    }

    @Test
    fun createUser_OK() {
        every { accountRepository.findOneByEmail(eq("new@user.com")) } returns null
        every { accountRoleRepository.findById(eq(54)) } returns Optional.of(AccountRole(54, "admin_role"))
        every { accountRepository.save(any<Account>()) } returnsArgument (0)

        val account = InputUserCreate(
            email = "new@user.com",
            name = "Ondrej",
            surname = "Tester",
            accountRoleId = 54
        )

        val result = userService.create(account)
        assertEquals("new@user.com", result.email)
        assertEquals("Ondrej", result.name)
        assertEquals("Tester", result.surname)
        assertEquals(OutputUserRole(54, "admin_role"), result.userRole)

        val event = slot<Audit>()
        verify { auditService.logEvent(capture(event)) }
        with(event.captured) {
            assertEquals("admin@admin.dev", username)
            assertEquals(AuditAction.USER_CREATED, action)
            assertEquals("new user new@user.com with role admin_role has been created by admin@admin.dev", description)
        }

    }

    @Test
    fun registerUser_OK() {
        every { accountRepository.findOneByEmail(eq("new@user.com")) } returns null
        every { accountRoleRepository.findOneByName(eq("applicant user")) } returns AccountRole(3, "applicant user")
        every { accountRepository.save(any<Account>()) } returnsArgument (0)

        val user = InputUserRegistration(
            email = "new@user.com",
            name = "Ondrej",
            surname = "Tester",
            password = "raw_password"
        )

        val result = userService.registerApplicant(user)
        assertEquals("new@user.com", result.email)
        assertEquals("Ondrej", result.name)
        assertEquals("Tester", result.surname)
        assertEquals(OutputUserRole(3, "applicant user"), result.userRole)

        val event = slot<Audit>()
        verify { auditService.logEvent(capture(event)) }
        with(event.captured) {
            assertEquals("new@user.com", username)
            assertEquals(AuditAction.USER_REGISTERED, action)
            assertEquals("new user 'Ondrej Tester' with role 'applicant user' registered", description)
        }

    }

}
