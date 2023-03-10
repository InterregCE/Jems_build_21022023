package io.cloudflight.jems.server.call.service.get_allow_real_costs

import io.cloudflight.jems.server.call.authorization.CanRetrieveCall
import io.cloudflight.jems.server.call.service.CallPersistence
import io.cloudflight.jems.server.call.service.model.AllowedRealCosts
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllowedRealCosts(private val persistence: CallPersistence) : GetAllowedRealCostsInteractor {

    @Transactional
    @CanRetrieveCall
    @ExceptionWrapper(GetAllowedRealCostsExceptions::class)
    override fun getAllowedRealCosts(callId: Long): AllowedRealCosts = persistence.getAllowedRealCosts(callId)
}
