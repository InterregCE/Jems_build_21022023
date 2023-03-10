package io.cloudflight.jems.server.project.service.report.project.base.createProjectReport

import io.cloudflight.jems.server.project.service.report.model.project.ProjectReport
import io.cloudflight.jems.server.project.service.report.model.project.ProjectReportUpdate

interface CreateProjectReportInteractor {

    fun createReportFor(projectId: Long, data: ProjectReportUpdate): ProjectReport

}
