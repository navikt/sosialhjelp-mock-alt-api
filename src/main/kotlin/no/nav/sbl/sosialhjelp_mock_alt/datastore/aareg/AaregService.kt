package no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsgiverType
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.OpplysningspliktigArbeidsgiverDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.OrganisasjonDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.PersonDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AaregService {

    final val aaregMap: HashMap<String, ArbeidsforholdDto> = HashMap()

    fun leggTilEnkeltArbeidsforhold(
            personalia: Personalia,
            startDato: LocalDate,
    ) {
        aaregMap.put(
                personalia.fnr,
                ArbeidsforholdDto.nyttArbeidsforhold(
                        personalia.fnr,
                        startDato,
                ))
    }

    fun leggTilArbeidsforhold(
            personalia: Personalia,
            startDato: LocalDate,
            sluttDato: LocalDate,
            stillingsprosent: Double,
            arbeidsforholdId: String,
            arbeidsforholdType: String,
            ident: String,
            orgnummer: String
    ) {
        if(personalia.locked) {
            throw RuntimeException("Ident ${personalia.fnr} is locked! Cannot update!")
        }
        val arbeidsgiver: OpplysningspliktigArbeidsgiverDto
        if(arbeidsforholdType === ArbeidsgiverType.PERSON.name) {
            arbeidsgiver = PersonDto(ident, ident)
        } else {
            arbeidsgiver = OrganisasjonDto(orgnummer)
        }
        aaregMap[personalia.fnr] = ArbeidsforholdDto.nyttArbeidsforhold(
                personalia.fnr,
                startDato,
                sluttDato,
                stillingsprosent,
                arbeidsgiver,
        )
    }

    fun getArbeidsforhold(fnr: String): List<ArbeidsforholdDto?> {
        return listOf(aaregMap[fnr])
    }

    companion object {
        private val log by logger()
    }
}
