package io.cloudflight.jems.server.programme.service.costoption

import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeLumpSum

interface ProgrammeLumpSumPersistence {

    fun getLumpSums(): List<ProgrammeLumpSum>
    fun getLumpSums(lumpSumIds: List<Long>): List<ProgrammeLumpSum>
    fun getLumpSum(lumpSumId: Long): ProgrammeLumpSum
    fun getCount(): Long
    fun createLumpSum(lumpSum: ProgrammeLumpSum): ProgrammeLumpSum
    fun updateLumpSum(lumpSum: ProgrammeLumpSum): ProgrammeLumpSum
    fun deleteLumpSum(lumpSumId: Long)
    fun getNumberOfOccurrencesInCalls(lumpSumId: Long): Int

}
