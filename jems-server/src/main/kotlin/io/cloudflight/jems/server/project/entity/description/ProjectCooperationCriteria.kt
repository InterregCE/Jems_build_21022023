package io.cloudflight.jems.server.project.entity.description

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class ProjectCooperationCriteria(

    @Column(nullable = false)
    val projectJointDevelopment: Boolean = false,

    @Column
    val projectJointDevelopmentDescription: String? = null,

    @Column(nullable = false)
    val projectJointImplementation: Boolean = false,

    @Column
    val projectJointImplementationDescription: String? = null,

    @Column(nullable = false)
    val projectJointStaffing: Boolean = false,

    @Column
    val projectJointStaffingDescription: String? = null,

    @Column(nullable = false)
    val projectJointFinancing: Boolean = false,

    @Column
    val projectJointFinancingDescription: String? = null

) {
    fun ifNotEmpty(): ProjectCooperationCriteria? {
        if (projectJointDevelopment
            || !projectJointDevelopmentDescription.isNullOrEmpty()
            || projectJointImplementation
            || !projectJointImplementationDescription.isNullOrEmpty()
            || projectJointStaffing
            || !projectJointStaffingDescription.isNullOrEmpty()
            || projectJointFinancing
            || !projectJointFinancingDescription.isNullOrEmpty()
        )
            return this
        return null
    }
}