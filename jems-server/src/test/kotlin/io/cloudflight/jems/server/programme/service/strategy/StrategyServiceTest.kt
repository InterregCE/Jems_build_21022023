package io.cloudflight.jems.server.programme.service.strategy

import io.cloudflight.jems.api.call.dto.CallStatus
import io.cloudflight.jems.api.programme.dto.strategy.InputProgrammeStrategy
import io.cloudflight.jems.api.programme.dto.strategy.OutputProgrammeStrategy
import io.cloudflight.jems.api.programme.dto.strategy.ProgrammeStrategy
import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.call.repository.CallRepository
import io.cloudflight.jems.server.programme.entity.ProgrammeStrategyEntity
import io.cloudflight.jems.server.programme.repository.StrategyRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher

internal class StrategyServiceTest {
    companion object {
        val strategy = ProgrammeStrategyEntity(
            strategy = ProgrammeStrategy.EUStrategyAdriaticIonianRegion,
            active = false
        )
        val strategySelected = ProgrammeStrategyEntity(
            strategy = ProgrammeStrategy.AtlanticStrategy,
            active = true
        )
    }

    @MockK
    lateinit var strategyRepository: StrategyRepository

    @MockK
    lateinit var callRepository: CallRepository

    @RelaxedMockK
    lateinit var auditPublisher: ApplicationEventPublisher

    lateinit var strategyService: StrategyService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        strategyService = StrategyServiceImpl(
            strategyRepository,
            callRepository,
            auditPublisher
        )
    }

    @Test
    fun get() {
        every { callRepository.existsByStatus(CallStatus.PUBLISHED) } returns false
        every { strategyRepository.findAll() } returns listOf(strategy)
        assertThat(strategyService.getProgrammeStrategies()).isEqualTo(
            listOf(
                OutputProgrammeStrategy(
                    strategy = strategy.strategy,
                    active = strategy.active
                )
            )
        )
    }

    @Test
    fun `update existing - active`() {
        every { callRepository.existsByStatus(CallStatus.PUBLISHED) } returns false
        every { strategyRepository.saveAll(any<List<ProgrammeStrategyEntity>>()) } returnsArgument 0
        val expectedResult = listOf(strategy.copy(active = true))
        every { strategyRepository.count() } returns expectedResult.size.toLong()
        every { strategyRepository.findAll() } returns expectedResult

        val existingFundToSelect =
            InputProgrammeStrategy(strategy = ProgrammeStrategy.EUStrategyAdriaticIonianRegion, active = true)
        val result = strategyService.save(listOf(existingFundToSelect))
        assertThat(result).isEqualTo(
            listOf(
                OutputProgrammeStrategy(
                    strategy = ProgrammeStrategy.EUStrategyAdriaticIonianRegion,
                    active = true
                )
            )
        )

        val audit = slot<AuditCandidateEvent>()
        verify { auditPublisher.publishEvent(capture(audit)) }
        with(audit) {
            assertThat(captured.auditCandidate.action).isEqualTo(AuditAction.PROGRAMME_STRATEGIES_CHANGED)
            assertThat(captured.auditCandidate.description).isEqualTo("Programme strategies was set to:\nEUStrategyAdriaticIonianRegion")
        }
    }

    @Test
    fun `update existing - restricted on call publish`() {
        val newInputStrategies = listOf(
            InputProgrammeStrategy(strategy = ProgrammeStrategy.EUStrategyAdriaticIonianRegion, active = false),
            InputProgrammeStrategy(strategy = ProgrammeStrategy.AtlanticStrategy, active = false)
        )
        val oldStrategies = listOf(strategy, strategySelected)

        every { callRepository.existsByStatus(CallStatus.PUBLISHED) } returns true
        every { strategyRepository.findAll() } returns oldStrategies

        assertThrows<UpdateStrategiesWhenProgrammeSetupRestricted> { strategyService.save(newInputStrategies) }
    }
}
