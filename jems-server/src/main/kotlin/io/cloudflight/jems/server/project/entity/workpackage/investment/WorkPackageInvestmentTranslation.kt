package io.cloudflight.jems.server.project.entity.workpackage.investment

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Embeddable
class WorkPackageInvestmentTranslation<T>(


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id")
    @field:NotNull
    val investment: T,


    @Enumerated(EnumType.STRING)
    @field:NotNull
    val language: SystemLanguage

) : Serializable {
    override fun equals(other: Any?) =
        this === other ||
                other !== null &&
                other is WorkPackageInvestmentTranslation<*> &&
                investment == other.investment &&
                language == other.language

    override fun hashCode() =
        if (investment === null) super.hashCode()
        else investment.hashCode().plus(language.translationKey.hashCode())

}
