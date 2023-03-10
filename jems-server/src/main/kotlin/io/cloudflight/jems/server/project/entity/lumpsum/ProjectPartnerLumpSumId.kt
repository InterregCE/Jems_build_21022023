package io.cloudflight.jems.server.project.entity.lumpsum

import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Embeddable
data class ProjectPartnerLumpSumId(

    @Embedded
    val projectLumpSumId: ProjectLumpSumId,

    @ManyToOne(optional = false)
    @field:NotNull
    val projectPartner: ProjectPartnerEntity,
) : Serializable {

    override fun equals(other: Any?): Boolean = (other is ProjectPartnerLumpSumId)
        && projectLumpSumId == other.projectLumpSumId
        && projectPartner.id == other.projectPartner.id

    override fun hashCode(): Int = projectLumpSumId.hashCode()
}
