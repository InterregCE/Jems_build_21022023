package io.cloudflight.jems.server.project.service.contracting.fileManagement.downloadPartnerFile

interface DownloadPartnerFileInteractor {

    fun downloadPartnerFile(projectId: Long, fileId: Long): Pair<String, ByteArray>
}
