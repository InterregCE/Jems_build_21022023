package io.cloudflight.ems.api.dto

import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class InputProject(
    @field:NotBlank(message = "project.acronym.should.not.be.empty")
    @field:Size(max = 25, message = "project.acronym.size.too.long")
    val acronym: String?,

    @field:NotNull(message = "project.submissionDate.should.not.be.empty")
    val submissionDate: LocalDate?
)
