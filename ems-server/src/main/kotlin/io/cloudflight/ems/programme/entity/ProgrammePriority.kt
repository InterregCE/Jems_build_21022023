package io.cloudflight.ems.programme.entity

import io.cloudflight.ems.api.programme.dto.ProgrammeObjective
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity(name = "programme_priority")
data class ProgrammePriority(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false, unique = true)
    val title: String,

    @Column(name = "objective_id", nullable = false)
    @Enumerated(EnumType.STRING)
    val objective: ProgrammeObjective,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "programme_priority_id", nullable = false, insertable = true)
    val programmePriorityPolicies: Set<ProgrammePriorityPolicy>

)