package io.cloudflight.jems.server.programme.service.checklist.get_detail_checklist

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.programme.service.checklist.ProgrammeChecklistPersistence
import io.cloudflight.jems.server.programme.service.checklist.getDetail.GetProgrammeChecklistDetail
import io.cloudflight.jems.server.programme.service.checklist.model.ProgrammeChecklistDetail
import io.cloudflight.jems.server.programme.service.checklist.model.ProgrammeChecklistType
import io.cloudflight.jems.server.project.service.checklist.ChecklistInstancePersistence
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

internal class GetChecklistDetailTest : UnitTest() {

    private val CHECKLIST_ID = 100L

    private val checklist = ProgrammeChecklistDetail(
        id = CHECKLIST_ID,
        type = ProgrammeChecklistType.APPLICATION_FORM_ASSESSMENT,
        name = "name",
        minScore = BigDecimal(0),
        maxScore = BigDecimal(10),
        allowsDecimalScore = false,
        lastModificationDate = ZonedDateTime.of(2020, 1, 10, 10, 10, 10, 10, ZoneId.systemDefault()),
        locked = false,
        components = emptyList()
    )

    @MockK
    lateinit var persistence: ProgrammeChecklistPersistence

    @MockK
    lateinit var checklistInstancePersistence: ChecklistInstancePersistence

    @InjectMockKs
    lateinit var getProgrammeChecklist: GetProgrammeChecklistDetail

    @Test
    fun getChecklistDetail() {
        every { checklistInstancePersistence.countAllByChecklistTemplateId(CHECKLIST_ID) } returns 0
        every { persistence.getChecklistDetail(CHECKLIST_ID) } returns checklist
        assertThat(getProgrammeChecklist.getProgrammeChecklistDetail(CHECKLIST_ID))
            .usingRecursiveComparison()
            .isEqualTo(checklist)
    }

}
