package io.cloudflight.jems.server.project.repository.partner.budget

import io.cloudflight.jems.server.project.entity.partner.budget.general.infrastructure.ProjectPartnerBudgetInfrastructureEntity
import org.springframework.stereotype.Repository

@Repository
interface ProjectPartnerBudgetInfrastructureRepository :
    ProjectPartnerBaseBudgetRepository<ProjectPartnerBudgetInfrastructureEntity> {

    fun existsByUnitCostId(unitCostId: Long): Boolean

}
