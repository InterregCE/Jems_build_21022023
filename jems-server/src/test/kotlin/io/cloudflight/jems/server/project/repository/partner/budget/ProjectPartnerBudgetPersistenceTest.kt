package io.cloudflight.jems.server.project.repository.partner.budget

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeUnitCostEntity
import io.cloudflight.jems.server.programme.repository.costoption.ProgrammeUnitCostRepository
import io.cloudflight.jems.server.project.entity.ProjectPeriodEntity
import io.cloudflight.jems.server.project.entity.ProjectPeriodId
import io.cloudflight.jems.server.project.repository.description.ProjectPeriodRepository
import io.cloudflight.jems.server.project.service.partner.model.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*


class ProjectPartnerBudgetPersistenceTest: UnitTest() {


        private  val partnerId = 1L
        private  val projectId = 2L
        private val projectPeriodId = ProjectPeriodId(projectId, 3)

        private val staffCostEntry = BudgetStaffCostEntry(
            id = 1,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.TEN,
            rowSum = BigDecimal.TEN,
            type = StaffCostType.REAL_COST,
            budgetPeriods = mutableSetOf(),
            unitType = StaffCostUnitType.HOUR
        )

        private val generalCostEntry = BudgetGeneralCostEntry(
            id = 1,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.TEN,
            budgetPeriods = mutableSetOf(),
            rowSum = BigDecimal.TEN
        )

        private val travelCostEntry = BudgetTravelAndAccommodationCostEntry(
            id = 1,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.TEN,
            rowSum = BigDecimal.TEN,
            budgetPeriods = mutableSetOf(),
            unitType = emptySet()
        )

        private val unitCostEntry = BudgetUnitCostEntry(
            id = 1,
            unitCostId = 1,
            numberOfUnits = BigDecimal.ONE,
            budgetPeriods = mutableSetOf(),
            rowSum = BigDecimal.TEN
        )
        private val programmeUnitCostEntity = ProgrammeUnitCostEntity(
            id = 1,
            name = "test",
            costPerUnit = BigDecimal.TEN,
            type = "test"
        )

        private val projectPeriodEntity = ProjectPeriodEntity(projectPeriodId, 1, 2)


    @RelaxedMockK
    lateinit var budgetStaffCostRepository: ProjectPartnerBudgetStaffCostRepository

    @RelaxedMockK
    lateinit var budgetTravelRepository: ProjectPartnerBudgetTravelRepository

    @RelaxedMockK
    lateinit var budgetExternalRepository: ProjectPartnerBudgetExternalRepository

    @RelaxedMockK
    lateinit var budgetEquipmentRepository: ProjectPartnerBudgetEquipmentRepository

    @RelaxedMockK
    lateinit var budgetInfrastructureRepository: ProjectPartnerBudgetInfrastructureRepository

    @RelaxedMockK
    lateinit var budgetUnitCostRepository: ProjectPartnerBudgetUnitCostRepository

    @RelaxedMockK
    lateinit var programmeUnitCostRepository: ProgrammeUnitCostRepository

    @RelaxedMockK
    lateinit var projectPeriodRepository: ProjectPeriodRepository

    @InjectMockKs
    private lateinit var persistence: ProjectPartnerBudgetCostsUpdatePersistenceProvider

    @Test
    fun `deletes all not excluded staff costs`() {
        val keepIds = listOf(2L)
        every {
            budgetStaffCostRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllBudgetStaffCostsExceptFor(partnerId, keepIds.toSet())
        verify { budgetStaffCostRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId, keepIds.toSet()) }
    }

    @Test
    fun `deletes all not excluded equipment costs`() {
        val keepIds = listOf(2L)
        every {
            budgetEquipmentRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllBudgetEquipmentCostsExceptFor(partnerId, keepIds.toSet())
        verify { budgetEquipmentRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId, keepIds.toSet()) }
    }

    @Test
    fun `deletes all not excluded external costs`() {
        val keepIds = listOf(2L)
        every {
            budgetExternalRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllBudgetExternalExpertiseAndServicesCostsExceptFor(partnerId, keepIds.toSet())
        verify { budgetExternalRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId, keepIds.toSet()) }
    }

    @Test
    fun `deletes all not excluded infrastructure costs`() {
        val keepIds = listOf(2L)
        every {
            budgetInfrastructureRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllBudgetInfrastructureAndWorksCostsExceptFor(partnerId, keepIds.toSet())
        verify {
            budgetInfrastructureRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        }
    }

    @Test
    fun `deletes all not excluded travel costs`() {
        val keepIds = listOf(2L)
        every {
            budgetTravelRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllBudgetTravelAndAccommodationCostsExceptFor(partnerId, keepIds.toSet())
        verify { budgetTravelRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId, keepIds.toSet()) }
    }

    @Test
    fun `deletes all not excluded unit costs`() {
        val keepIds = setOf(2L)
        every {
            budgetUnitCostRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(
                partnerId,
                keepIds.toSet()
            )
        } returns Unit
        persistence.deleteAllUnitCostsExceptFor(partnerId, keepIds.toSet())
        verify { budgetUnitCostRepository.deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId, keepIds.toSet()) }
    }

    @Test
    fun `updates staff costs`() {
        val partnerBudgetEntity = staffCostEntry.toProjectPartnerBudgetStaffCostEntity(
            partnerId
        ){projectPeriodEntity}
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetStaffCostRepository.saveAll(listOf(partnerBudgetEntity)) } returns listOf(partnerBudgetEntity)
        val result = persistence.createOrUpdateBudgetStaffCosts(projectId, partnerId, setOf(staffCostEntry))
        assertThat(result).isEqualTo(listOf(staffCostEntry))
    }

    @Test
    fun `updates equipment costs`() {
        val partnerBudgetEntity = generalCostEntry.toProjectPartnerBudgetEquipmentEntity(
            partnerId
        ){projectPeriodEntity}
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetEquipmentRepository.saveAll(listOf(partnerBudgetEntity)) } returns listOf(partnerBudgetEntity)
        val result = persistence.createOrUpdateBudgetEquipmentCosts(projectId, partnerId, setOf(generalCostEntry))
        assertThat(result).isEqualTo(listOf(generalCostEntry))
    }

    @Test
    fun `updates external and service costs`() {
        val partnerBudgetEntity = generalCostEntry.toProjectPartnerBudgetExternalEntity(
            partnerId
        ) { projectPeriodEntity }
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetExternalRepository.saveAll(listOf(partnerBudgetEntity)) } returns listOf(partnerBudgetEntity)
        val result =
            persistence.createOrUpdateBudgetExternalExpertiseAndServicesCosts(projectId, partnerId, setOf(generalCostEntry))
        assertThat(result).isEqualTo(listOf(generalCostEntry))
    }

    @Test
    fun `updates infrastructure costs`() {
        val partnerBudgetEntity = generalCostEntry.toProjectPartnerBudgetInfrastructureEntity(
            partnerId
        ) { projectPeriodEntity }
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetInfrastructureRepository.saveAll(listOf(partnerBudgetEntity)) } returns listOf(partnerBudgetEntity)
        val result =
            persistence.createOrUpdateBudgetInfrastructureAndWorksCosts(projectId, partnerId, setOf(generalCostEntry))
        assertThat(result).isEqualTo(listOf(generalCostEntry))
    }

    @Test
    fun `updates travel costs`() {
        val partnerBudgetEntity = travelCostEntry.toProjectPartnerBudgetTravelEntity(
            partnerId
        ) { projectPeriodEntity }
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetTravelRepository.saveAll(listOf(partnerBudgetEntity)) } returns listOf(partnerBudgetEntity)
        val result =
            persistence.createOrUpdateBudgetTravelAndAccommodationCosts(projectId, partnerId, setOf(travelCostEntry))
        assertThat(result).isEqualTo(listOf(travelCostEntry))
    }

    @Test
    fun `updates unit costs`() {
        every { programmeUnitCostRepository.findById(programmeUnitCostEntity.id) } returns Optional.of(
            programmeUnitCostEntity
        )
        val partnerBudgetEntities =
            setOf(unitCostEntry).toBudgetUnitCostEntities(
                partnerId,
                { programmeUnitCostEntity },
                { projectPeriodEntity }
            )
        every {  programmeUnitCostRepository.getOne(programmeUnitCostEntity.id)} returns programmeUnitCostEntity
        every {  projectPeriodRepository.getOne(projectPeriodId)} returns projectPeriodEntity
        every { budgetUnitCostRepository.saveAll(partnerBudgetEntities) } returns partnerBudgetEntities

        val result = persistence.createOrUpdateBudgetUnitCosts(projectId, partnerId, setOf(unitCostEntry))
        assertThat(result).isEqualTo(listOf(unitCostEntry))
    }

}
