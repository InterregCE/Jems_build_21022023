package io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.getBeneficialOwners

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectContractingPartner
import io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.ContractingPartnerBeneficialOwner
import io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.ContractingPartnerBeneficialOwnersPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetContractingPartnerBeneficialOwners(
    private val beneficialOwnersPersistence: ContractingPartnerBeneficialOwnersPersistence
): GetContractingPartnerBeneficialOwnersInteractor {

    @CanRetrieveProjectContractingPartner
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetContractingPartnerBeneficialOwnersException::class)
    override fun getBeneficialOwners(partnerId: Long): List<ContractingPartnerBeneficialOwner> {
        return beneficialOwnersPersistence.getBeneficialOwners(partnerId)
    }
}
