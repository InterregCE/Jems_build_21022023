package io.cloudflight.jems.server.programme.repository.costoption

import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeLumpSumEntity
import io.cloudflight.jems.server.programme.service.costoption.ProgrammeLumpSumPersistence
import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeLumpSum
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProgrammeLumpSumPersistenceProvider(
    private val repository: ProgrammeLumpSumRepository
) : ProgrammeLumpSumPersistence {

    @Transactional(readOnly = true)
    override fun getLumpSums(): List<ProgrammeLumpSum> =
        repository.findTop100ByOrderById().toModel()

    @Transactional(readOnly = true)
    override fun getLumpSums(lumpSumIds: List<Long>): List<ProgrammeLumpSum> =
        repository.findAllByIdIn(lumpSumIds).toModel()

    @Transactional(readOnly = true)
    override fun getLumpSum(lumpSumId: Long): ProgrammeLumpSum =
        getLumpSumOrThrow(lumpSumId).toModel()

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional
    override fun createLumpSum(lumpSum: ProgrammeLumpSum): ProgrammeLumpSum {
        val created = repository.save(lumpSum.toEntity())
        return repository.save(
            created.copy(
                translatedValues = combineLumpSumTranslatedValues(created.id, lumpSum.name, lumpSum.description),
                categories = lumpSum.categories.toEntity(created.id)
            )
        ).toModel()
    }

    @Transactional
    override fun updateLumpSum(lumpSum: ProgrammeLumpSum): ProgrammeLumpSum {
        if (repository.existsById(lumpSum.id)) {
            return repository.save(
                lumpSum.toEntity().copy(
                    translatedValues = combineLumpSumTranslatedValues(lumpSum.id, lumpSum.name, lumpSum.description),
                    categories = lumpSum.categories.toEntity(lumpSum.id)
                )
            ).toModel()
        } else throw ResourceNotFoundException("programmeLumpSum")
    }

    @Transactional
    override fun deleteLumpSum(lumpSumId: Long) =
        repository.delete(getLumpSumOrThrow(lumpSumId))

    @Transactional
    override fun getNumberOfOccurrencesInCalls(lumpSumId: Long): Int =
        repository.getNumberOfOccurrencesInCalls(lumpSumId)

    private fun getLumpSumOrThrow(lumpSumId: Long): ProgrammeLumpSumEntity =
        repository.findById(lumpSumId).orElseThrow { ResourceNotFoundException("programmeLumpSum") }

}
