package io.cloudflight.jems.api.project.dto.description

/**
 * C
 */
data class OutputProjectDescription(
    val projectOverallObjective: InputProjectOverallObjective?,    // C1
    val projectRelevance: InputProjectRelevance?,    // C2
    val projectPartnership: InputProjectPartnership?,    // C3
    val projectManagement: OutputProjectManagement?,    // C7
    val projectLongTermPlans: OutputProjectLongTermPlans?    // C8
)
