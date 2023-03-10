package io.cloudflight.jems.server.project.service.contracting.fileManagement.downloadContractFile

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.project.authorization.CanViewContractsAndAgreements
import io.cloudflight.jems.server.project.service.contracting.fileManagement.FileNotFound
import io.cloudflight.jems.server.project.service.contracting.fileManagement.ProjectContractingFilePersistence
import io.cloudflight.jems.server.project.service.contracting.fileManagement.validateContractFile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DownloadContractFile(
    private val contractingFilePersistence: ProjectContractingFilePersistence,
    private val filePersistence: JemsFilePersistence,
) : DownloadContractFileInteractor {

    @CanViewContractsAndAgreements
    @Transactional(readOnly = true)
    @ExceptionWrapper(DownloadContractFileException::class)
    override fun download(projectId: Long, fileId: Long): Pair<String, ByteArray> {
        validateContractFile(filePersistence.getFileType(fileId, projectId))
        return contractingFilePersistence.downloadFile(projectId = projectId, fileId = fileId)
            ?: throw FileNotFound()
    }
}
