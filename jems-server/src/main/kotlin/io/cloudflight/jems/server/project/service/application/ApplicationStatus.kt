package io.cloudflight.jems.server.project.service.application

enum class ApplicationStatus {
    STEP1_DRAFT,
    STEP1_SUBMITTED,
    STEP1_ELIGIBLE,
    STEP1_INELIGIBLE,
    STEP1_APPROVED,
    STEP1_APPROVED_WITH_CONDITIONS,
    STEP1_NOT_APPROVED,
    DRAFT,
    SUBMITTED,
    RETURNED_TO_APPLICANT,
    ELIGIBLE,
    INELIGIBLE,
    APPROVED,
    APPROVED_WITH_CONDITIONS,
    NOT_APPROVED;

    fun isNotSubmittedNow() =
        this == DRAFT || this == STEP1_DRAFT || this == RETURNED_TO_APPLICANT

    fun isInStepTwo() =
        setOf(
            DRAFT, SUBMITTED, RETURNED_TO_APPLICANT,
            ELIGIBLE, INELIGIBLE,
            APPROVED, APPROVED_WITH_CONDITIONS, NOT_APPROVED
        ).contains(this)

}
