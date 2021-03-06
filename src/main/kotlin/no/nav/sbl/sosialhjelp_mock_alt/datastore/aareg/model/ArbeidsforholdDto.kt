package no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigOrganisasjonsnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import java.time.LocalDate

data class ArbeidsforholdDto(
    val ansettelsesperiode: AnsettelsesperiodeDto,
    val arbeidsavtaler: List<ArbeidsavtaleDto>,
    val arbeidsforholdId: String,
    val arbeidsgiver: OpplysningspliktigArbeidsgiverDto,
    val arbeidstaker: PersonDto,
    val navArbeidsforholdId: Long,
) {
    companion object {
        fun nyttArbeidsforhold(
            fnr: String,
            fom: LocalDate,
            tom: LocalDate? = null,
            stillingsprosent: Double = 100.0,
            arbeidsforholdId: String = randomInt(7).toString(),
            arbeidsgiver: OpplysningspliktigArbeidsgiverDto = OrganisasjonDto(
                organisasjonsnummer = genererTilfeldigOrganisasjonsnummer(),
            ),
            navArbeidsforholdId: Long = randomInt(7).toLong(),
        ): ArbeidsforholdDto {
            val person = PersonDto(
                offentligIdent = fnr,
                aktoerId = fnr,
                type = "Person"
            )
            return ArbeidsforholdDto(
                AnsettelsesperiodeDto(
                    PeriodeDto(
                        fom = fom,
                        tom = tom
                    )
                ),
                listOf(ArbeidsavtaleDto(stillingsprosent)),
                arbeidsforholdId = arbeidsforholdId,
                arbeidsgiver = arbeidsgiver,
                arbeidstaker = person,
                navArbeidsforholdId = navArbeidsforholdId
            )
        }
    }
}

enum class ArbeidsgiverType {
    Person,
    Organisasjon,
}

class AnsettelsesperiodeDto(val periode: PeriodeDto)

class PeriodeDto(val fom: LocalDate, val tom: LocalDate?)

class ArbeidsavtaleDto(val stillingsprosent: Double)

interface OpplysningspliktigArbeidsgiverDto {
    val type: String
}

class OrganisasjonDto(val organisasjonsnummer: String, override val type: String = "Organisasjon") : OpplysningspliktigArbeidsgiverDto

class PersonDto(val offentligIdent: String, val aktoerId: String, override val type: String = "Person") : OpplysningspliktigArbeidsgiverDto
