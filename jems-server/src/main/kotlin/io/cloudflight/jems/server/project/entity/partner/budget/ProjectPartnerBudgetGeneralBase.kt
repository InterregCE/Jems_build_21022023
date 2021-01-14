package io.cloudflight.jems.server.project.entity.partner.budget

interface ProjectPartnerBudgetGeneralBase {
    val id: Long
    val baseProperties: BaseBudgetProperties
    val investmentId: Long?
    val translatedValues: MutableSet<out ProjectPartnerBudgetGeneralTranslBase>
}