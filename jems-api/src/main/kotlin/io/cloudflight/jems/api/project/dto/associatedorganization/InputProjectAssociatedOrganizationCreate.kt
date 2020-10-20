package io.cloudflight.jems.api.project.dto.associatedorganization

import io.cloudflight.jems.api.project.dto.InputOrganization
import io.cloudflight.jems.api.project.dto.InputProjectContact
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class  InputProjectAssociatedOrganizationCreate(

    @field:NotNull(message = "project.partner.should.not.be.empty")
    val partnerId: Long,

    @field:Size(max = 100, message = "partner.organization.original.name.size.too.long")
    @field:NotNull(message = "project.organization.original.should.not.be.empty")
    override val nameInOriginalLanguage: String? = null,

    @field:Size(max = 100, message = "partner.organization.english.name.size.too.long")
    @field:NotNull(message = "project.organization.english.should.not.be.empty")
    override val nameInEnglish: String? = null,

    val address: InputProjectAssociatedOrganizationAddress? = null,

    @field:Size(max = 2, message = "project.associatedOrganization.contacts.size.too.long")
    val contacts: List<InputProjectContact> = emptyList()

): InputOrganization