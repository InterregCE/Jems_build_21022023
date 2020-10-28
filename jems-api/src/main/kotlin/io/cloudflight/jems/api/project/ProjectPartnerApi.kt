package io.cloudflight.jems.api.project

import io.cloudflight.jems.api.project.dto.InputProjectContact
import io.cloudflight.jems.api.project.dto.InputProjectPartnerContribution
import io.cloudflight.jems.api.project.dto.partner.InputProjectPartnerCreate
import io.cloudflight.jems.api.project.dto.partner.InputProjectPartnerAddress
import io.cloudflight.jems.api.project.dto.partner.InputProjectPartnerUpdate
import io.cloudflight.jems.api.project.dto.partner.OutputProjectPartner
import io.cloudflight.jems.api.project.dto.partner.OutputProjectPartnerDetail
import io.cloudflight.jems.api.project.dto.partner.cofinancing.InputProjectPartnerCoFinancingWrapper
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.DeleteMapping
import javax.validation.Valid

@Api("Project Partner")
@RequestMapping("/api/project/{projectId}/partner")
interface ProjectPartnerApi {

    @ApiOperation("Returns all project partners")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string", allowMultiple = true)
    )
    @GetMapping
    fun getProjectPartners(@PathVariable projectId: Long, pageable: Pageable): Page<OutputProjectPartner>

    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string", allowMultiple = true)
    )
    @GetMapping("/ids")
    fun getProjectPartnersForDropdown(@PathVariable projectId: Long, pageable: Pageable): List<OutputProjectPartner>

    @ApiOperation("Returns a project partner by id")
    @GetMapping("/{partnerId}")
    fun getProjectPartnerById(@PathVariable projectId: Long, @PathVariable partnerId: Long): OutputProjectPartnerDetail

    @ApiOperation("Creates new project partner")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createProjectPartner(
        @PathVariable projectId: Long,
        @Valid @RequestBody projectPartner: InputProjectPartnerCreate
    ): OutputProjectPartnerDetail

    @ApiOperation("Update project partner")
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectPartner(
        @PathVariable projectId: Long,
        @Valid @RequestBody projectPartner: InputProjectPartnerUpdate
    ): OutputProjectPartnerDetail

    @ApiOperation("Update project partner addresses")
    @PutMapping("/{partnerId}/address", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectPartnerAddress(
        @PathVariable projectId: Long,
        @PathVariable partnerId: Long,
        @Valid @RequestBody addresses: Set<InputProjectPartnerAddress>
    ): OutputProjectPartnerDetail

    @ApiOperation("Update project partner contact")
    @PutMapping("/{partnerId}/contact", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectPartnerContact(
        @PathVariable projectId: Long,
        @PathVariable partnerId: Long,
        @Valid @RequestBody contacts: Set<InputProjectContact>
    ): OutputProjectPartnerDetail

    @ApiOperation("Update project partner contribution")
    @PutMapping("/{partnerId}/contribution", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectPartnerContribution(
        @PathVariable projectId: Long,
        @PathVariable partnerId: Long,
        @Valid @RequestBody partnerContribution: InputProjectPartnerContribution
    ): OutputProjectPartnerDetail

    @ApiOperation("Update project partner co-financing")
    @PutMapping("/{partnerId}/cofinancing", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectPartnerCoFinancing(
        @PathVariable projectId: Long,
        @PathVariable partnerId: Long,
        @Valid @RequestBody partnerCoFinancing: InputProjectPartnerCoFinancingWrapper
    ): OutputProjectPartnerDetail

    @ApiOperation("Delete a project partner")
    @DeleteMapping("/{partnerId}")
    fun deleteProjectPartner(@PathVariable projectId: Long, @PathVariable partnerId: Long)

}
