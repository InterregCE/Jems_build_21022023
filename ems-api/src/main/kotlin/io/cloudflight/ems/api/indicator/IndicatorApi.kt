package io.cloudflight.ems.api.indicator

import io.cloudflight.ems.api.indicator.dto.InputIndicatorOutputCreate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorOutputUpdate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorResultCreate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorResultUpdate
import io.cloudflight.ems.api.indicator.dto.OutputIndicatorOutput
import io.cloudflight.ems.api.indicator.dto.OutputIndicatorResult
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
import javax.validation.Valid

@Api("Programme Indicator")
@RequestMapping("/api/programmeindicator")
interface IndicatorApi {

    //region INDICATOR OUTPUT

    @ApiOperation("Returns all OUTPUT indicators")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping("/output")
    fun getAllIndicatorOutput(pageable: Pageable): Page<OutputIndicatorOutput>

    @ApiOperation("Returns OUTPUT indicator by id")
    @GetMapping("/output/{id}")
    fun getIndicatorOutput(@PathVariable id: Long): OutputIndicatorOutput

    @ApiOperation("Creates new OUTPUT indicator")
    @PostMapping("/output", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createIndicatorOutput(@Valid @RequestBody indicator: InputIndicatorOutputCreate): OutputIndicatorOutput

    @ApiOperation("Updates existing OUTPUT indicator")
    @PutMapping("/output", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateIndicatorOutput(@Valid @RequestBody indicator: InputIndicatorOutputUpdate): OutputIndicatorOutput
    //endregion

    //region INDICATOR RESULT

    @ApiOperation("Returns all RESULT indicators")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping("/result")
    fun getAllIndicatorResult(pageable: Pageable): Page<OutputIndicatorResult>

    @ApiOperation("Returns RESULT indicator by id")
    @GetMapping("/result/{id}")
    fun getIndicatorResult(@PathVariable id: Long): OutputIndicatorResult

    @ApiOperation("Creates new RESULT indicator")
    @PostMapping("/result", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createIndicatorResult(@Valid @RequestBody indicator: InputIndicatorResultCreate): OutputIndicatorResult

    @ApiOperation("Updates existing RESULT indicator")
    @PutMapping("/result", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateIndicatorResult(@Valid @RequestBody indicator: InputIndicatorResultUpdate): OutputIndicatorResult
    //endregion

}