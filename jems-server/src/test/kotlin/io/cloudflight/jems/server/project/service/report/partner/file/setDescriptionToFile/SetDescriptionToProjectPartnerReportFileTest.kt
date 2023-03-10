package io.cloudflight.jems.server.project.service.report.partner.file.setDescriptionToFile

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.JemsProjectFileService
import io.cloudflight.jems.server.common.validator.AppInputValidationException
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SetDescriptionToProjectPartnerReportFileTest : UnitTest() {

    companion object {
        private const val expectedPath = "Project/000008/Report/Partner/000640/PartnerReport/000477/"
    }

    @MockK
    lateinit var partnerPersistence: PartnerPersistence
    @MockK
    lateinit var filePersistence: JemsFilePersistence

    @MockK
    lateinit var fileService: JemsProjectFileService

    @MockK
    lateinit var generalValidator: GeneralValidatorService

    @InjectMockKs
    lateinit var interactor: SetDescriptionToProjectPartnerReportFile

    @BeforeEach
    fun setup() {
        clearMocks(generalValidator, filePersistence, fileService)
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isEmpty() }) } returns Unit
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isNotEmpty() }) } throws
            AppInputValidationException(emptyMap())
        every { generalValidator.maxLength(any<String>(), 250, "description") } returns emptyMap()
    }

    @Test
    fun setDescription() {
        val partnerId = 640L
        val projectId = 8L
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { filePersistence.existsFile(partnerId, expectedPath, 200L) } returns true
        every { fileService.setDescription(200L, "new desc") } answers { }

        interactor.setDescription(partnerId, reportId = 477L, fileId = 200L, "new desc")
        verify(exactly = 1) { fileService.setDescription(200L, "new desc") }
    }

    @Test
    fun `setDescription - not existing`() {
        val partnerId = 645L
        val projectId = 9L
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { filePersistence
            .existsFile(partnerId, "Project/000009/Report/Partner/000645/PartnerReport/000000/", -1L)
        } returns false

        assertThrows<FileNotFound> { interactor.setDescription(partnerId, 0L, fileId = -1L, "") }
    }

}
