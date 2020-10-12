package io.cloudflight.jems.server.project.repository

import io.cloudflight.jems.api.project.dto.ProjectPartnerRole
import io.cloudflight.jems.server.project.entity.ProjectPartner
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProjectPartnerRepository : PagingAndSortingRepository<ProjectPartner, Long> {

    @EntityGraph(attributePaths = ["partnerContactPersons"])
    override fun findById(id: Long): Optional<ProjectPartner>

    @EntityGraph(attributePaths = ["partnerContactPersons"])
    fun findFirstByProjectIdAndId(projectId: Long, id: Long): Optional<ProjectPartner>

    fun findAllByProjectId(projectId: Long, pageable: Pageable): Page<ProjectPartner>

    fun findAllByProjectId(projectId: Long, sort: Sort): Iterable<ProjectPartner>

    fun findFirstByProjectIdAndRole(projectId: Long, role: ProjectPartnerRole): Optional<ProjectPartner>

}