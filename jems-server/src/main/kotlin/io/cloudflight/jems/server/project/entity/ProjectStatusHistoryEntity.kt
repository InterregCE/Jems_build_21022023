package io.cloudflight.jems.server.project.entity

import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.user.entity.UserEntity
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Objects
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity(name = "project_status")
data class ProjectStatusHistoryEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    var project: ProjectEntity? = null,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    var status: ApplicationStatus,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    @field:NotNull
    val user: UserEntity,

    @field:NotNull
    val updated: ZonedDateTime = ZonedDateTime.now(),

    var decisionDate: LocalDate? = null,

    var entryIntoForceDate: LocalDate? = null,

    var note: String? = null

) {
    override fun toString(): String {
        return "${this.javaClass.simpleName}(status=$status, user=$user, updated=$updated, note=$note)"
    }

    override fun equals(other: Any?): Boolean = (other is ProjectStatusHistoryEntity)
        && project?.id == other.project?.id
        && status == other.status
        && updated == other.updated

    override fun hashCode(): Int = Objects.hash(project?.id, status)

}
