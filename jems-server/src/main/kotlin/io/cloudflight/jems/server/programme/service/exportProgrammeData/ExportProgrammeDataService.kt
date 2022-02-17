package io.cloudflight.jems.server.programme.service.exportProgrammeData

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.server.plugin.services.toDataModel
import io.cloudflight.jems.server.programme.service.model.ProgrammeDataExportMetadata
import io.cloudflight.jems.server.programme.service.userrole.ProgrammeDataPersistence
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class ExportProgrammeDataService(private val programmeDataPersistence: ProgrammeDataPersistence) {

    @Async
    @Transactional
    fun execute(plugin: ProgrammeDataExportPlugin, exportLanguage: SystemLanguage, inputLanguage: SystemLanguage) {
        runCatching {
            plugin.export(exportLanguage.toDataModel(), inputLanguage.toDataModel()).also { result ->
                programmeDataPersistence.saveExportFile(plugin.getKey(), result.content, true).also {
                    programmeDataPersistence.updateExportMetaData(
                        plugin.getKey(), result.fileName, result.contentType,
                        result.startTime, result.endTime ?: ZonedDateTime.now()
                    )
                }
            }
        }.onFailure { programmeDataPersistence.deleteExportMetaData(plugin.getKey()) }
    }

    @Transactional
    fun saveExportFileMetaData(pluginKey: String, exportLanguage: SystemLanguage, inputLanguage: SystemLanguage){
            with(programmeDataPersistence.listExportMetadata()) {
                throwIfAnyExportIsInProgress(this)
                deleteMetadataIfAlreadyExist(this, pluginKey)
            }
            programmeDataPersistence.saveExportMetaData(pluginKey, exportLanguage, inputLanguage, ZonedDateTime.now())
    }

    private fun throwIfAnyExportIsInProgress(metadataList: List<ProgrammeDataExportMetadata>) {
        if (metadataList.firstOrNull {
                it.exportEndedAt == null && it.requestTime.isAfter(
                    ZonedDateTime.now().minusMinutes(EXPORT_TIMEOUT_IN_MINUTES)
                )
            } != null
        ) throw ExportInProgressException()
    }

    private fun deleteMetadataIfAlreadyExist(metadataList: List<ProgrammeDataExportMetadata>, pluginKey: String) {
        if (metadataList.firstOrNull { it.pluginKey == pluginKey } != null)
            programmeDataPersistence.deleteExportMetaData(pluginKey)
    }
}
