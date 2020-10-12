package io.cloudflight.jems.server.call.controller

import io.cloudflight.jems.api.call.CallApi
import io.cloudflight.jems.api.call.dto.InputCallCreate
import io.cloudflight.jems.api.call.dto.InputCallUpdate
import io.cloudflight.jems.api.call.dto.OutputCall
import io.cloudflight.jems.api.call.dto.OutputCallList
import io.cloudflight.jems.api.call.dto.OutputCallProgrammePriority
import io.cloudflight.jems.server.call.service.CallService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class CallController(
    private val callService: CallService
) : CallApi {

    /**
     * Here the @PreAuthorize annotation is missing because list is filtered based on restrictions inside the service
     */
    override fun getCalls(pageable: Pageable): Page<OutputCallList> {
        return callService.getCalls(pageable)
    }

    @PreAuthorize("@callAuthorization.canReadCallDetail(#id)")
    override fun getCallById(id: Long): OutputCall {
        return callService.getCallById(id)
    }

    @PreAuthorize("@callAuthorization.canCreateCall()")
    override fun createCall(call: InputCallCreate): OutputCall {
        return callService.createCall(call)
    }

    @PreAuthorize("@callAuthorization.canUpdateCall(#call.id)")
    override fun updateCall(call: InputCallUpdate): OutputCall {
        return callService.updateCall(call)
    }

    @PreAuthorize("@callAuthorization.canUpdateCall(#id)")
    override fun publishCall(id: Long): OutputCall {
        return callService.publishCall(id)
    }

    @PreAuthorize("@callAuthorization.canReadCallDetail(#id)")
    override fun getCallObjectives(id: Long): List<OutputCallProgrammePriority> {
        return callService.getPriorityAndPoliciesForCall(id)
    }

}