package io.cloudflight.jems.server.audit.service

import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class AuditListener(
    private val auditService: AuditService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun pushAuditAfterSuccessfulTransaction(event: AuditCandidateEvent) {
        auditService.logEvent(event.auditCandidate)
    }

}