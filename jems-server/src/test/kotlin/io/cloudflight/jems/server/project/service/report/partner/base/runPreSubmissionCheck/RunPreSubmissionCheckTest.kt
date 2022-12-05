package io.cloudflight.jems.server.project.service.report.partner.base.runPreSubmissionCheck

import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.report.ProjectReportPersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RunPreSubmissionCheckTest : UnitTest() {

    @MockK
    private lateinit var reportPersistence: ProjectReportPersistence
    @MockK
    private lateinit var service: RunPreSubmissionCheckService

    @InjectMockKs
    private lateinit var interactor: RunPreSubmissionCheck

    @BeforeEach
    fun reset() {
        clearMocks(reportPersistence)
        clearMocks(service)
    }

    @Test
    fun preCheck() {
        every { reportPersistence.exists(partnerId = 4L, reportId = 48L) } returns true
        val result = mockk<PreConditionCheckResult>()
        every { service.preCheck(partnerId = 4L, reportId = 48L) } returns result

        assertThat(interactor.preCheck(partnerId = 4L, reportId = 48L)).isEqualTo(result)
    }

    @Test
    fun `preCheck - not existing`() {
        every { reportPersistence.exists(partnerId = 1L, reportId = -1L) } returns false
        assertThrows<ReportNotFound> { interactor.preCheck(partnerId = 1L, reportId = -1L) }
        verify(exactly = 0) { service.preCheck(any(), any()) }
    }

}