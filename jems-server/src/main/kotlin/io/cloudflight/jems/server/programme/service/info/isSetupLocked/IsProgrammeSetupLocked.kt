package io.cloudflight.jems.server.programme.service.info.isSetupLocked

import io.cloudflight.jems.server.call.repository.CallPersistenceProvider
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.programme.authorization.CanRetrieveProgrammeSetup
import io.cloudflight.jems.server.project.service.lumpsum.ProjectLumpSumPersistence
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IsProgrammeSetupLocked(
    private val callPersistence: CallPersistenceProvider,
    private val projectReportPersistence: ProjectPartnerReportPersistence,
    private val projectLumpSumPersistence: ProjectLumpSumPersistence
) : IsProgrammeSetupLockedInteractor {

    @Transactional(readOnly = true)
    @CanRetrieveProgrammeSetup
    @ExceptionWrapper(IsProgrammeSetupLockedException::class)
    override fun isLocked(): Boolean =
        callPersistence.hasAnyCallPublished()


    @Transactional(readOnly = true)
    @CanRetrieveProgrammeSetup
    @ExceptionWrapper(IsProgrammeSetupLockedException::class)
    override fun isAnyReportCreated(): Boolean =
        projectReportPersistence.isAnyReportCreated()

    @Transactional(readOnly = true)
    @CanRetrieveProgrammeSetup
    @ExceptionWrapper(IsProgrammeSetupLockedException::class)
    override fun isFastTrackLumpSumReadyForPayment(programmeLumpSumId: Long): Boolean =
        projectLumpSumPersistence.isFastTrackLumpSumReadyForPayment(programmeLumpSumId)
}
