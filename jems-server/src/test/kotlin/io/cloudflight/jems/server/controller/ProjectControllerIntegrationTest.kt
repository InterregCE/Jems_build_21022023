package io.cloudflight.jems.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudflight.jems.api.project.dto.InputProject
import io.cloudflight.jems.server.call.entity.ApplicationFormConfigurationEntity
import io.cloudflight.jems.server.call.repository.ApplicationFormConfigurationRepository
import io.cloudflight.jems.server.factory.CallFactory
import io.cloudflight.jems.server.factory.UserFactory
import io.cloudflight.jems.server.factory.UserFactory.Companion.ADMINISTRATOR_EMAIL
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var callFactory: CallFactory

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var jsonMapper: ObjectMapper

    @Autowired
    private lateinit var applicationFormConfigurationRepository: ApplicationFormConfigurationRepository

    @Test
    @WithUserDetails(value = ADMINISTRATOR_EMAIL)

    fun `project created`() {
        applicationFormConfigurationRepository.save(ApplicationFormConfigurationEntity(1L, "test configuration"))
        val call = callFactory.savePublishedCallWithoutPolicy(userFactory.adminUser)
        val inputProject = InputProject(acronym = "acronym", projectCallId = call.id)
        mockMvc.perform(
            post("/api/project")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(inputProject))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.acronym").value(inputProject.acronym.toString()))
    }

    @Test
    @WithUserDetails(value = ADMINISTRATOR_EMAIL)
    fun `project create fails with missing required fields`() {
        val call = callFactory.savePublishedCallWithoutPolicy(userFactory.adminUser)
        val inputProject = InputProject(null, call.id)

        mockMvc.perform(
            post("/api/project")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(inputProject))
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(
                jsonPath("$.formErrors.acronym.i18nKey")
                    .value("common.error.field.blank")
            )
    }

    @Test
    @WithUserDetails(value = ADMINISTRATOR_EMAIL)
    fun `project create fails with invalid fields`() {
        val call = callFactory.savePublishedCallWithoutPolicy(userFactory.adminUser)
        val inputProject = InputProject(RandomString.make(26), call.id)

        mockMvc.perform(
            post("/api/project")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(inputProject))
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(
                jsonPath("$.formErrors.acronym.i18nKey")
                    .value("common.error.field.max.length")
            )
    }
}
