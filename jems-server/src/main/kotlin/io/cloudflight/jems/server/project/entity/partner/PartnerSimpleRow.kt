package io.cloudflight.jems.server.project.entity.partner

import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRole

interface PartnerSimpleRow {
    val id: Long?
    val abbreviation: String
    val role: ProjectPartnerRole
    val sortNumber: Int?
    val country: String?
}