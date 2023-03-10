package io.cloudflight.jems.server.project.controller.workpackage

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.CS
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.EN
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.SK
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.api.project.dto.workpackage.output.WorkPackageOutputDTO
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.workpackage.output.get_work_package_output.GetWorkPackageOutputInteractor
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput
import io.cloudflight.jems.server.project.service.workpackage.output.update_work_package_output.UpdateWorkPackageOutputInteractor
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProjectWorkPackageOutputControllerTest : UnitTest() {


        private val output1 = WorkPackageOutput(
            workPackageId = 1L,
            outputNumber = 1,
            title = setOf(
                InputTranslation(language = EN, translation = null),
                InputTranslation(language = CS, translation = ""),
                InputTranslation(language = SK, translation = "sk_title"),
            ),
            description = setOf(
                InputTranslation(language = EN, translation = "en_desc"),
                InputTranslation(language = CS, translation = null),
                InputTranslation(language = SK, translation = "sk_desc"),
            ),
            periodNumber = 1,
            programmeOutputIndicatorId = 50L,
            programmeOutputIndicatorIdentifier = "ID.50",
            targetValue = BigDecimal.ONE,
            deactivated = false,
        )
        private val output2 = WorkPackageOutput(
            workPackageId = 1L,
            outputNumber = 2,
            title = emptySet(),
            description = emptySet(),
            periodNumber = 3,
            deactivated = false,
        )

    @MockK
    lateinit var getOutputInteractor: GetWorkPackageOutputInteractor

    @MockK
    lateinit var updateOutputInteractor: UpdateWorkPackageOutputInteractor

    @InjectMockKs
    private lateinit var controller: ProjectWorkPackageOutputController

    @Test
    fun getOutputs() {
        every { getOutputInteractor.getOutputsForWorkPackage(1L, 1L) } returns listOf(output1, output2)

        assertThat(controller.getOutputs(1L, 1L)).containsExactly(
            WorkPackageOutputDTO(
                workPackageId = 1L,
                outputNumber = 1,
                title = setOf(InputTranslation(EN, null), InputTranslation(CS, ""), InputTranslation(SK, "sk_title")),
                periodNumber = 1,
                programmeOutputIndicatorId = 50L,
                programmeOutputIndicatorIdentifier = "ID.50",
                description = setOf(
                    InputTranslation(EN, "en_desc"), InputTranslation(CS, null), InputTranslation(SK, "sk_desc")
                ),
                targetValue = BigDecimal.ONE,
            ),
            WorkPackageOutputDTO(
                workPackageId = 1L,
                outputNumber = 2,
                periodNumber = 3,
            ),
        )
    }

    @Test
    fun `updateOutputs - test if persistence method is called with correct arguments`() {
        val outputsSlot = slot<List<WorkPackageOutput>>()
        every { updateOutputInteractor.updateOutputsForWorkPackage(1L, 1L, capture(outputsSlot)) } returnsArgument 2

        val outputDto1 = WorkPackageOutputDTO(
            workPackageId = 1L,
            title = setOf(InputTranslation(EN, null), InputTranslation(CS, ""), InputTranslation(SK, "sk_title")),
            periodNumber = 1,
            programmeOutputIndicatorId = 15,
            description = setOf(
                InputTranslation(EN, "en_desc"),
                InputTranslation(CS, ""),
                InputTranslation(SK, "sk_desc")
            ),
            targetValue = BigDecimal.ONE,
        )
        val outputDto2 = WorkPackageOutputDTO(
            workPackageId = 1L,
            periodNumber = 3,
            targetValue = null,
        )

        controller.updateOutputs(1L, 1L, listOf(outputDto1, outputDto2))

        assertThat(outputsSlot.captured).containsExactly(
            WorkPackageOutput(
                workPackageId = 1L,
                outputNumber = 0,
                title = setOf(
                    InputTranslation(language = EN, translation = null),
                    InputTranslation(language = CS, translation = ""),
                    InputTranslation(language = SK, translation = "sk_title"),
                ),
                description = setOf(
                    InputTranslation(language = EN, translation = "en_desc"),
                    InputTranslation(language = CS, translation = ""),
                    InputTranslation(language = SK, translation = "sk_desc"),
                ),
                periodNumber = 1,
                programmeOutputIndicatorId = 15,
                targetValue = BigDecimal.ONE,
                deactivated = false,
            ),
            WorkPackageOutput(
                workPackageId = 1L,
                outputNumber = 0,
                periodNumber = 3,
                deactivated = false,
            )
        )
    }

}
