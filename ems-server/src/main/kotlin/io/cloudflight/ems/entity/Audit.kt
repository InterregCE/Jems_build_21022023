package io.cloudflight.ems.entity

import io.cloudflight.ems.api.dto.ProjectApplicationStatus
import io.cloudflight.ems.api.dto.ProjectEligibilityAssessmentResult
import io.cloudflight.ems.api.dto.ProjectQualityAssessmentResult
import io.cloudflight.ems.api.dto.call.OutputCall
import io.cloudflight.ems.api.dto.user.OutputUser
import io.cloudflight.ems.api.dto.user.OutputUserWithRole
import io.cloudflight.ems.security.model.CurrentUser
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Document(indexName = "audit-log", type = "audit")
data class Audit(
    @Id
    @Field(type = FieldType.Text)
    val id: String? = null,

    @Field(type = FieldType.Date, store = true, format = DateFormat.date_time)
    val timestamp: String? = getElasticTimeNow(),

    @Field(type = FieldType.Keyword, store = true)
    val action: AuditAction?,

    @Field(type = FieldType.Keyword, store = true)
    val projectId: String?,

    @Field(type = FieldType.Object, store = true)
    val user: AuditUser?,

    @Field(type = FieldType.Text, store = true, index = false)
    val description: String?
) {

    companion object {

        fun projectStatusChanged(
            currentUser: CurrentUser?,
            projectId: String,
            oldStatus: ProjectApplicationStatus? = null,
            newStatus: ProjectApplicationStatus
        ): Audit {
            val msg = if (oldStatus == null && newStatus == ProjectApplicationStatus.DRAFT)
                "Project application created with status $newStatus"
            else
                "Project application status changed from $oldStatus to $newStatus"
            return Audit(
                timestamp = getElasticTimeNow(),
                action = AuditAction.APPLICATION_STATUS_CHANGED,
                projectId = projectId,
                user = currentUser?.toEsUser(),
                description = msg
            )
        }

        fun projectFileDeleted(currentUser: CurrentUser?, projectId: Long, file: ProjectFile): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.PROJECT_FILE_DELETED,
                projectId = projectId.toString(),
                user = currentUser?.toEsUser(),
                description = "document ${file.name} deleted from application $projectId"
            )
        }

        fun projectFileUploadedSuccessfully(currentUser: CurrentUser?, projectId: Long, file: ProjectFile): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.PROJECT_FILE_UPLOADED_SUCCESSFULLY,
                projectId = projectId.toString(),
                user = currentUser?.toEsUser(),
                description = "document ${file.name} uploaded to project application $projectId"
            )
        }

        fun projectFileUploadFailed(currentUser: CurrentUser?, projectId: Long, fileName: String?): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.PROJECT_FILE_UPLOAD_FAILED,
                projectId = projectId.toString(),
                user = currentUser?.toEsUser(),
                description = "FAILED upload of document $fileName to project application $projectId"
            )
        }

        fun projectFileDescriptionChanged(currentUser: CurrentUser?, projectId: Long, file: ProjectFile, oldDescription: String?): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.PROJECT_FILE_DESCRIPTION_CHANGED,
                projectId = projectId.toString(),
                user = currentUser?.toEsUser(),
                description = "description of document ${file.name} in project application $projectId has changed from $oldDescription to ${file.description}"
            )
        }

        fun userLoggedIn(user: AuditUser): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.USER_LOGGED_IN,
                projectId = null,
                user = user,
                description = "user with email ${user.email} logged in"
            )
        }

        fun userLoggedOut(user: AuditUser): Audit {
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.USER_LOGGED_OUT,
                projectId = null,
                user = user,
                description = "user with email ${user.email} logged out"
            )
        }

        fun userCreated(currentUser: CurrentUser?, createdUser: OutputUserWithRole): Audit {
            val author = currentUser?.user?.email
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.USER_CREATED,
                projectId = null,
                user = currentUser?.toEsUser(),
                description = "new user ${createdUser.email} with role ${createdUser.userRole.name} has been created by $author"
            )
        }

        fun userRoleChanged(currentUser: CurrentUser?, user: OutputUserWithRole): Audit {
            val author = currentUser?.user?.email
            return Audit(
                id = null,
                timestamp = getElasticTimeNow(),
                action = AuditAction.USER_ROLE_CHANGED,
                projectId = null,
                user = currentUser?.toEsUser(),
                description = "user role '${user.userRole.name}' has been assigned to ${user.name} ${user.surname} by $author"
            )
        }

        fun userDataChanged(currentUser: CurrentUser?, userId: Long, changes: Map<String, Pair<String, String>>): Audit {
            val changedString = changes.entries.stream()
                .map { "${it.key} changed from ${it.value.first} to ${it.value.second}" }
                .collect(Collectors.joining(",\n"))

            return Audit(
                action = AuditAction.USER_DATA_CHANGED,
                projectId = null,
                user = currentUser?.toEsUser(),
                description = "User data changed for user $userId:\n$changedString"
            )
        }

        fun applicantRegistered(createdUser: OutputUserWithRole): Audit {
            return Audit(
                action = AuditAction.USER_REGISTERED,
                projectId = null,
                user = AuditUser(createdUser.id!!, createdUser.email),
                description = "new user '${createdUser.name} ${createdUser.surname}' with role '${createdUser.userRole.name}' registered"
            )
        }

        fun userSessionExpired(user: AuditUser): Audit {
            return Audit(
                action = AuditAction.USER_SESSION_EXPIRED,
                projectId = null,
                user = user,
                description = "user with email ${user.email} was logged out by the system"
            )
        }

        fun passwordChanged(initiator: CurrentUser?, changedUser: OutputUser): Audit {
            return Audit(
                action = AuditAction.PASSWORD_CHANGED,
                projectId = null,
                user = initiator?.toEsUser(),
                description = if (initiator?.user?.id == changedUser.id)
                    "Password of user '${changedUser.name} ${changedUser.surname}' (${changedUser.email}) has been changed by himself/herself"
                else "Password of user '${changedUser.name} ${changedUser.surname}' (${changedUser.email}) has been changed by user ${initiator?.user?.email}"
            )
        }

        fun qualityAssessmentConcluded(currentUser: CurrentUser?, projectId: String, result: ProjectQualityAssessmentResult): Audit {
            return Audit(
                action = AuditAction.QUALITY_ASSESSMENT_CONCLUDED,
                projectId = projectId,
                user = currentUser?.toEsUser(),
                description = "Project application quality assessment concluded as $result"
            )
        }

        fun eligibilityAssessmentConcluded(currentUser: CurrentUser?, projectId: String, result: ProjectEligibilityAssessmentResult): Audit {
            return Audit(
                action = AuditAction.ELIGIBILITY_ASSESSMENT_CONCLUDED,
                projectId = projectId,
                user = currentUser?.toEsUser(),
                description = "Project application eligibility assessment concluded as $result"
            )
        }

        fun callCreated(currentUser: CurrentUser?, callId: String, call: OutputCall): Audit {
            return Audit(
                action = AuditAction.CALL_CREATED,
                projectId = callId,
                user = currentUser?.toEsUser(),
                description = "New call '${call.name}' was created by ${currentUser?.user?.email}"
            )
        }

        fun callUpdated(currentUser: CurrentUser?, callId: String, call: OutputCall): Audit {
            return Audit(
                action = AuditAction.CALL_UPDATED,
                projectId = callId,
                user = currentUser?.toEsUser(),
                description = "Call '${call.name}' was updated by ${currentUser?.user?.email}"
            )
        }

        private fun getElasticTimeNow(): String {
            return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)
        }

    }

    // default constructor is needed for deserialization
    constructor() : this(null, null, null, null, null, null)

}
