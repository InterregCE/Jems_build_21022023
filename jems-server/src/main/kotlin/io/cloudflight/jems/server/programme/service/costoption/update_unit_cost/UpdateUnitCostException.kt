package io.cloudflight.jems.server.programme.service.costoption.update_unit_cost

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationAccessDeniedException
import io.cloudflight.jems.server.common.exception.ApplicationException

const val UPDATE_UNIT_COST_ERROR_CODE_PREFIX = "S-UNC-UUC"
const val UPDATE_UNIT_COST_ERROR_KEY_PREFIX = "use.case.update.unit.cost"

class UpdateUnitCostException(cause: Throwable) : ApplicationException(
    code = UPDATE_UNIT_COST_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$UPDATE_UNIT_COST_ERROR_KEY_PREFIX.failed"), cause = cause
)

class UpdateUnitCostWhenProgrammeSetupRestricted : ApplicationAccessDeniedException(
    code = "$UPDATE_UNIT_COST_ERROR_CODE_PREFIX-001",
    i18nMessage = I18nMessage("$UPDATE_UNIT_COST_ERROR_KEY_PREFIX.programme.setup.restricted"),
)
