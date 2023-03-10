package io.cloudflight.jems.server.project.repository.report.partner.workPlan

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityTranslEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageTranslEntity
import io.cloudflight.jems.server.project.repository.report.partner.ProjectPartnerReportRepository
import io.cloudflight.jems.server.project.repository.report.partner.toModel
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.ProjectPartnerReportWorkPackage
import io.cloudflight.jems.server.project.service.report.partner.workPlan.ProjectPartnerReportWorkPlanPersistence
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProjectPartnerReportWorkPlanPersistenceProvider(
    private val partnerReportRepository: ProjectPartnerReportRepository,
    private val workPlanRepository: ProjectPartnerReportWorkPackageRepository,
    private val workPlanActivityRepository: ProjectPartnerReportWorkPackageActivityRepository,
    private val workPlanActivityDeliverableRepository: ProjectPartnerReportWorkPackageActivityDeliverableRepository,
    private val workPlanOutputRepository: ProjectPartnerReportWorkPackageOutputRepository,
) : ProjectPartnerReportWorkPlanPersistence {

    @Transactional(readOnly = true)
    override fun getPartnerReportWorkPlanById(partnerId: Long, reportId: Long): List<ProjectPartnerReportWorkPackage> {
        val reportEntity = partnerReportRepository.findByIdAndPartnerId(id = reportId, partnerId = partnerId)

        val activitiesByWorkPackage = workPlanActivityRepository
            .findAllByWorkPackageEntityReportEntityOrderByNumber(reportEntity)
            .groupBy { it.workPackageEntity }
        val deliverablesByActivity = workPlanActivityDeliverableRepository
            .findAllByActivityEntityWorkPackageEntityReportEntityOrderByNumber(reportEntity)
            .groupBy { it.activityEntity }
        val outputsByWorkPackage = workPlanOutputRepository
            .findAllByWorkPackageEntityReportEntityOrderByNumber(reportEntity)
            .groupBy { it.workPackageEntity }

        return workPlanRepository.findAllByReportEntityOrderByNumber(reportEntity).toModel(
            retrieveActivities = { wp -> activitiesByWorkPackage[wp] ?: emptyList() },
            retrieveDeliverables = { activity -> deliverablesByActivity[activity] ?: emptyList() },
            retrieveOutputs = { wp -> outputsByWorkPackage[wp] ?: emptyList() },
        )
    }

    @Transactional(readOnly = true)
    override fun existsByActivityId(partnerId: Long, reportId: Long, workPackageId: Long, activityId: Long) =
        workPlanActivityRepository.existsByActivityId(activityId = activityId,
            workPackageId = workPackageId, reportId = reportId, partnerId = partnerId)

    @Transactional(readOnly = true)
    override fun existsByDeliverableId(partnerId: Long, reportId: Long, workPackageId: Long, activityId: Long, deliverableId: Long) =
        workPlanActivityDeliverableRepository
            .existsByDeliverableId(deliverableId = deliverableId, activityId = activityId,
                workPackageId = workPackageId, reportId = reportId, partnerId = partnerId)

    @Transactional(readOnly = true)
    override fun existsByOutputId(partnerId: Long, reportId: Long, workPackageId: Long, outputId: Long) =
        workPlanOutputRepository.existsByOutputId(outputId = outputId,
            workPackageId = workPackageId, reportId = reportId, partnerId = partnerId)

    @Transactional
    override fun updatePartnerReportWorkPackage(workPackageId: Long, translations: Set<InputTranslation>) {
        val toBeUpdatedLanguages = translations.associateBy({ it.language }, { it.translation })

        workPlanRepository.findById(workPackageId).get().apply {
            addMissingLanguagesIfNeeded(languages = toBeUpdatedLanguages.keys)

            translatedValues.forEach {
                it.description = toBeUpdatedLanguages[it.language()]
            }
        }
    }

    private fun ProjectPartnerReportWorkPackageEntity.addMissingLanguagesIfNeeded(languages: Set<SystemLanguage>) {
        val existingLanguages = translatedValues.mapTo(HashSet()) { it.translationId.language }
        languages.filter { !existingLanguages.contains(it) }.forEach { language ->
            translatedValues.add(
                ProjectPartnerReportWorkPackageTranslEntity(
                    translationId = TranslationId(this, language = language),
                    description = null,
                )
            )
        }
    }

    @Transactional
    override fun updatePartnerReportWorkPackageActivity(activityId: Long, translations: Set<InputTranslation>) {
        val toBeUpdatedLanguages = translations.associateBy({ it.language }, { it.translation })

        workPlanActivityRepository.findById(activityId).get().apply {
            addMissingLanguagesIfNeeded(languages = toBeUpdatedLanguages.keys)

            // update values of all languages
            translatedValues.forEach {
                it.description = toBeUpdatedLanguages[it.language()]
            }
        }
    }

    private fun ProjectPartnerReportWorkPackageActivityEntity.addMissingLanguagesIfNeeded(languages: Set<SystemLanguage>) {
        val existingLanguages = translatedValues.mapTo(HashSet()) { it.translationId.language }
        languages.filter { !existingLanguages.contains(it) }.forEach { language ->
            translatedValues.add(
                ProjectPartnerReportWorkPackageActivityTranslEntity(
                    translationId = TranslationId(this, language = language),
                    title = null,
                    description = null,
                )
            )
        }
    }

    @Transactional
    override fun updatePartnerReportWorkPackageDeliverable(
        deliverableId: Long,
        contribution: Boolean?,
        evidence: Boolean?,
    ) {
        workPlanActivityDeliverableRepository.findById(deliverableId).get().apply {
            this.contribution = contribution
            this.evidence = evidence
        }
    }

    @Transactional
    override fun updatePartnerReportWorkPackageOutput(outputId: Long, contribution: Boolean?, evidence: Boolean?) {
        workPlanOutputRepository.findById(outputId).get().apply {
            this.contribution = contribution
            this.evidence = evidence
        }
    }

}
