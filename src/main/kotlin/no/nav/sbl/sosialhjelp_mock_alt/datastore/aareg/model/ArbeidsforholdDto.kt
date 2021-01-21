package no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import java.time.LocalDate

class ArbeidsforholdDto(
        val ansettelsesperiode: AnsettelsesperiodeDto,
        val arbeidsavtaler: List<ArbeidsavtaleDto>,
        val arbeidsforholdId: String,
        val arbeidsgiver: OpplysningspliktigArbeidsgiverDto,
        val arbeidstaker: PersonDto,
        val navArbeidsforholdId: Long,
) {
    companion object {
        fun opprettDummyForhold(fnr: String): ArbeidsforholdDto {
            val person = PersonDto(
                    offentligIdent = fnr,
                    aktoerId = fnr,
                    type = "Person"
            )
            return ArbeidsforholdDto(
                    AnsettelsesperiodeDto(
                            PeriodeDto(
                                    fom = LocalDate.now().minusYears(10),
                                    tom = LocalDate.now()
                            )
                    ),
                    listOf(ArbeidsavtaleDto(100.0)),
                    arbeidsforholdId = randomInt(7).toString(),
                    arbeidsgiver = OrganisasjonDto(
                                    organisasjonsnummer = randomInt(9).toString(),
                    ),
                    arbeidstaker = person,
                    navArbeidsforholdId = randomInt(7).toLong()
            )
        }

        fun nyttArbeidsforhold(
                personalia: Personalia,
                fom: LocalDate,
                tom: LocalDate? = null,
                stillingsprosent: Double = 100.0,
                arbeidsgiver: OpplysningspliktigArbeidsgiverDto = OrganisasjonDto(
                        organisasjonsnummer = randomInt(9).toString(),
                ),
                navArbeidsforholdId: Long = randomInt(7).toLong(),
        ): ArbeidsforholdDto {
            val person = PersonDto(
                    offentligIdent = personalia.fnr,
                    aktoerId = personalia.fnr,
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
                    arbeidsforholdId = randomInt(7).toString(),
                    arbeidsgiver = arbeidsgiver,
                    arbeidstaker = person,
                    navArbeidsforholdId = navArbeidsforholdId
            )
        }
    }
}

enum class ArbeidsgiverType {
    PERSON,
    ORGANISASJON,
}

class AnsettelsesperiodeDto(val periode: PeriodeDto)

class PeriodeDto(val fom: LocalDate, val tom: LocalDate?)

class ArbeidsavtaleDto(val stillingsprosent: Double)

interface OpplysningspliktigArbeidsgiverDto {
    val type: String
}

class OrganisasjonDto(val organisasjonsnummer: String, override val type: String = "organisasjon"): OpplysningspliktigArbeidsgiverDto

class PersonDto(val offentligIdent: String, val aktoerId: String, override val type: String = "person"): OpplysningspliktigArbeidsgiverDto
