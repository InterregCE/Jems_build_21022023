package io.cloudflight.jems.api.project.dto.partner

import io.cloudflight.jems.api.project.dto.InputOrganization
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class InputProjectPartnerCreate(

    @field:NotBlank(message = "project.partner.name.should.not.be.empty")
    @field:Size(max = 15, message = "project.partner.name.size.too.long")
    val abbreviation: String?,

    @field:NotNull(message = "project.partner.role.should.not.be.empty")
    val role: ProjectPartnerRole?,

    /**
     * Optional: if creating new LeadPartner when there is already one (then it is mandatory)
     */
    val oldLeadPartnerId: Long? = null,

    @field:Size(max = 100, message = "project.organization.original.name.size.too.long")
    override val nameInOriginalLanguage: String? = null,

    @field:Size(max = 100, message = "project.organization.english.name.size.too.long")
    override val nameInEnglish: String? = null,

    @field:Size(max = 250, message = "project.organization.department.size.too.long")
    val department: String? = null

): InputOrganization
