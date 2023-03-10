package io.cloudflight.jems.server.audit.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.cloudflight.jems.server.audit.model.Audit
import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.model.AuditUser
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.project.authorization.AuthorizationUtil.Companion.applicantUser
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertLinesMatch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory


class AuditServiceTest {

    private val EXPECTED_LOG = "AUDIT >>> APPLICATION_STATUS_CHANGED (projectId 1, user (3, user@applicant.dev)) : status change from X to Y"

    @MockK
    lateinit var auditPersistence: AuditPersistence
    @MockK
    lateinit var securityService: SecurityService

    lateinit var auditService: AuditService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { securityService.currentUser } returns applicantUser
    }

    /**
     * Test audits, if they are saved in repository.
     */
    @Test
    fun testLogEvent_repository() {
        val event = slot<Audit>()
        every { auditPersistence.saveAudit(capture(event)) } returns "ID_of_audit"
        auditService = AuditServiceImpl(securityService, auditPersistence)

        // test start
        auditService.logEvent(AuditCandidate(
            action = AuditAction.APPLICATION_STATUS_CHANGED,
            project = AuditProject(1L.toString()),
            description = "status change from X to Y"
        ))

        // assert
        assertEquals(
            AuditAction.APPLICATION_STATUS_CHANGED, event.captured.action,
            "Correct audit log action should be assigned to audit log entry")
        assertEquals("1", event.captured.project?.id,
            "project ID should not be lost when creating audit log entry")
        assertEquals(
            AuditUser(applicantUser.user.id, applicantUser.user.email), event.captured.user,
            "Correct User should be assigned to audit log entry.")
        assertEquals("status change from X to Y", event.captured.description,
            "Description should be saved to audit log entry.")
    }

    /**
     * Test audits, if they are not saved in repository, but redirected to log.
     */
    @Test
    fun testLogEvent_logger() {
        auditService = AuditServiceLoggerImpl(securityService)

        val logger: Logger = LoggerFactory.getLogger(AuditServiceLoggerImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)

        // test start
        auditService.logEvent(AuditCandidate(
            action = AuditAction.APPLICATION_STATUS_CHANGED,
            project = AuditProject(1L.toString()),
            description = "status change from X to Y"
        ))

        // assert
        assertLinesMatch(listOf(EXPECTED_LOG), listAppender.list.map { it.formattedMessage })
        assertIterableEquals(listOf(Level.INFO), listAppender.list.map { it.level })
    }
}
