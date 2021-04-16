package io.cloudflight.jems.server.project.service

import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeUnitCost
import io.cloudflight.jems.server.project.service.model.ProjectCallSettings
import io.cloudflight.jems.server.project.service.model.ProjectPeriod
import io.cloudflight.jems.server.project.service.model.ProjectSummary

interface ProjectPersistence {

    fun getProjectSummary(projectId: Long): ProjectSummary

    fun getProjectCallSettings(projectId: Long): ProjectCallSettings

    fun getProjectUnitCosts(projectId: Long): List<ProgrammeUnitCost>

    fun getProjectPeriods(projectId: Long): List<ProjectPeriod>

    fun getProjectIdForPartner(partnerId: Long): Long
}
