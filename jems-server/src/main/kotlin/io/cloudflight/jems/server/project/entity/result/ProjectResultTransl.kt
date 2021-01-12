package io.cloudflight.jems.server.project.entity.result

import io.cloudflight.jems.server.project.entity.TranslationResultId
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity

/**
 * Project Result lang table
 */
@Entity(name = "project_result_transl")
data class ProjectResultTransl (
    @EmbeddedId
    val translationId: TranslationResultId,

    @Column
    val description: String? = null
)
