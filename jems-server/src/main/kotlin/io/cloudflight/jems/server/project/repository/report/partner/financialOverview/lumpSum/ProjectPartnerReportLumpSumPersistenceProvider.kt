package io.cloudflight.jems.server.project.repository.report.partner.financialOverview.lumpSum

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.project.repository.report.partner.expenditure.ProjectPartnerReportLumpSumRepository
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.lumpSum.ExpenditureLumpSumBreakdownLine
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportLumpSumPersistence
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
class ProjectPartnerReportLumpSumPersistenceProvider(
    private val reportLumpSumRepository: ProjectPartnerReportLumpSumRepository,
) : ProjectPartnerReportLumpSumPersistence {

    @Transactional(readOnly = true)
    override fun getLumpSum(partnerId: Long, reportId: Long) =
        reportLumpSumRepository
            .findByReportEntityPartnerIdAndReportEntityIdOrderByOrderNrAscIdAsc(partnerId = partnerId, reportId = reportId)
            .map { ExpenditureLumpSumBreakdownLine(
                reportLumpSumId = it.id,
                lumpSumId = it.programmeLumpSum.id,
                name = it.programmeLumpSum.translatedValues.mapTo(HashSet()) {
                    InputTranslation(it.translationId.language, it.name)
                },
                period = it.period,
                totalEligibleBudget = it.total,
                previouslyReported = it.previouslyReported,
                previouslyPaid = it.previouslyPaid,
                currentReport = it.current,
                totalEligibleAfterControl = it.totalEligibleAfterControl,
            ) }

    @Transactional(readOnly = true)
    override fun getLumpSumCumulative(reportIds: Set<Long>) =
        reportLumpSumRepository.findCumulativeForReportIds(reportIds).toMap()

    @Transactional
    override fun updateCurrentlyReportedValues(
        partnerId: Long,
        reportId: Long,
        currentlyReported: Map<Long, BigDecimal>,
    ) {
        reportLumpSumRepository
            .findByReportEntityPartnerIdAndReportEntityIdOrderByOrderNrAscIdAsc(partnerId = partnerId, reportId = reportId)
            .forEach {
                if (currentlyReported.containsKey(it.id)) {
                    it.current = currentlyReported.get(it.id)!!
                }
            }
    }

    @Transactional
    override fun updateAfterControlValues(partnerId: Long, reportId: Long, afterControl: Map<Long, BigDecimal>) {
        reportLumpSumRepository
            .findByReportEntityPartnerIdAndReportEntityIdOrderByOrderNrAscIdAsc(partnerId = partnerId, reportId = reportId)
            .forEach {
                if (afterControl.containsKey(it.id)) {
                    it.totalEligibleAfterControl = afterControl.get(it.id)!!
                }
            }
    }
}
