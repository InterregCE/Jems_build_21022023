package io.cloudflight.jems.server.programme.entity.typologyerrors

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity(name = "programme_typology_errors")
data class ProgrammeTypologyErrorsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotNull
    val description: String = ""
)
