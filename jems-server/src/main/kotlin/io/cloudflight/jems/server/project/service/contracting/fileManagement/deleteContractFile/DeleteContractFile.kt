package io.cloudflight.jems.server.project.service.contracting.fileManagement.deleteContractFile

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.project.authorization.CanEditContractsAndAgreements
import io.cloudflight.jems.server.project.service.contracting.fileManagement.ProjectContractingFilePersistence
import io.cloudflight.jems.server.project.service.contracting.fileManagement.validateContractFile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteContractFile(
    private val contractingFilePersistence: ProjectContractingFilePersistence,
    private val filePersistence: JemsFilePersistence
) : DeleteContractFileInteractor {

    @CanEditContractsAndAgreements
    @Transactional
    @ExceptionWrapper(DeleteContractFileException::class)
    override fun delete(projectId: Long, fileId: Long) {
        validateContractFile(filePersistence.getFileType(fileId, projectId))
        contractingFilePersistence.deleteFile(projectId = projectId, fileId = fileId)
    }
}
