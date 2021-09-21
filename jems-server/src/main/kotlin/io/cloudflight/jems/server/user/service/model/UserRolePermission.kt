package io.cloudflight.jems.server.user.service.model

enum class UserRolePermission(val key: String) {

    // Module PROGRAMME SETUP
    ProgrammeSetupRetrieve("ProgrammeSetupRetrieve"),
    ProgrammeSetupUpdate("ProgrammeSetupUpdate"),

    // Module CALL
    CallRetrieve("CallRetrieve"),
    CallPublishedRetrieve("CallPublishedRetrieve"),
    CallUpdate("CallUpdate"),

    // Module APPLICATIONS
    ProjectRetrieve("ProjectRetrieve"),
    ProjectsWithOwnershipRetrieve("ProjectsWithOwnershipRetrieve"),
    ProjectCreate("ProjectCreate"),

    ProjectFormRetrieve("ProjectFormRetrieve"),
    ProjectFormUpdate("ProjectFormUpdate"),

    ProjectFileApplicationRetrieve("ProjectFileApplicationRetrieve"),
    ProjectFileApplicationUpdate("ProjectFileApplicationUpdate"),
    ProjectFileAssessmentRetrieve("ProjectFileAssessmentRetrieve"),
    ProjectFileAssessmentUpdate("ProjectFileAssessmentUpdate"),

    // Module APPLICATION LIFECYCLE
    ProjectCheckApplicationForm("ProjectCheckApplicationForm"),
    ProjectSubmission("ProjectSubmission"),

    ProjectAssessmentView("ProjectAssessmentView"),
    ProjectAssessmentQualityEnter("ProjectAssessmentQualityEnter"),
    ProjectAssessmentEligibilityEnter("ProjectAssessmentEligibilityEnter"),
    ProjectStatusReturnToApplicant("ProjectStatusReturnToApplicant"),
    ProjectStatusDecideEligible("ProjectStatusDecideEligible"),
    ProjectStatusDecideIneligible("ProjectStatusDecideIneligible"),
    ProjectStatusDecideApproved("ProjectStatusDecideApproved"),
    ProjectStatusDecideApprovedWithConditions("ProjectStatusDecideApprovedWithConditions"),
    ProjectStatusDecideNotApproved("ProjectStatusDecideNotApproved"),
    ProjectStatusDecisionRevert("ProjectStatusDecisionRevert"),
    ProjectStartStepTwo("ProjectStartStepTwo"),

    // Module SYSTEM
    AuditRetrieve("AuditRetrieve"),

    RoleRetrieve("RoleRetrieve"),
    RoleCreate("RoleCreate"),
    RoleUpdate("RoleUpdate"),

    UserRetrieve("UserRetrieve"),
    UserCreate("UserCreate"),
    UserUpdate("UserUpdate"),
    UserUpdateRole("UserUpdateRole"),
    UserUpdatePassword("UserUpdatePassword"),
}
