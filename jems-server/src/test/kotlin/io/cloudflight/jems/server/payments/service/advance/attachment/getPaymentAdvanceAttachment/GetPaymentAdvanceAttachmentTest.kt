package io.cloudflight.jems.server.payments.service.advance.attachment.getPaymentAdvanceAttachment

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.report.ProjectReportFilePersistence
import io.cloudflight.jems.server.project.service.report.model.file.JemsFile
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class GetPaymentAdvanceAttachmentTest : UnitTest() {

    @MockK
    lateinit var reportFilePersistence: ProjectReportFilePersistence

    @InjectMockKs
    lateinit var interactor: GetPaymentAdvanceAttachment

    @BeforeEach
    fun reset() {
        clearMocks(reportFilePersistence)
    }

    @Test
    fun list() {
        val reportFile = mockk<JemsFile>()
        every {
            reportFilePersistence.listAttachments(
                pageable = any(),
                indexPrefix = "Payment/Advance/000004/PaymentAdvanceAttachment/",
                filterSubtypes = emptySet(),
                filterUserIds = emptySet(),
            )
        } returns PageImpl(listOf(reportFile))

        assertThat(interactor.list(4L, Pageable.unpaged()).content)
            .containsExactly(reportFile)
    }

}