package io.cloudflight.jems.server.project.service.checklist.create.control

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException
import io.cloudflight.jems.server.common.exception.ApplicationUnprocessableException

const val CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_CODE_PREFIX = "S-C-CHIC"
const val CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_KEY_PREFIX = "use.case.create.control.checklist.instance"

class CreateControlChecklistInstanceException(cause: Throwable) : ApplicationException(
    code = CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_KEY_PREFIX.failed"), cause = cause
)

class CreateControlChecklistInstanceStatusNotAllowedException : ApplicationUnprocessableException(
    code = "$CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_CODE_PREFIX-002",
    i18nMessage = I18nMessage("$CREATE_CONTROL_CHECKLIST_INSTANCE_ERROR_KEY_PREFIX.status.not.allowed")
)
