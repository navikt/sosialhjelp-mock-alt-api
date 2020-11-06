package no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
                    arbeidsforholdId = randomInt(5).toString(),
                    arbeidsgiver = OrganisasjonDto(
                                    organisasjonsnummer = randomInt(8).toString(),
                                    type = "Organisasjon"

                    ),
                    arbeidstaker = person,
                    navArbeidsforholdId = randomInt(5).toLong()
            )
        }
    }
}

class AnsettelsesperiodeDto(val periode: PeriodeDto)

class PeriodeDto(val fom: LocalDate, val tom: LocalDate)

class ArbeidsavtaleDto(val stillingsprosent: Double)

interface OpplysningspliktigArbeidsgiverDto

class OrganisasjonDto(val organisasjonsnummer: String, val type: String): OpplysningspliktigArbeidsgiverDto

class PersonDto(val offentligIdent: String, val aktoerId: String, val type: String): OpplysningspliktigArbeidsgiverDto
