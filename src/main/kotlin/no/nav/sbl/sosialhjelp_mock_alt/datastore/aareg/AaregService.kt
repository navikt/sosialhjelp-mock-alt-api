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

    final val aaregMap: HashMap<String, List<ArbeidsforholdDto>> = HashMap()

    fun leggTilEnkeltArbeidsforhold(
            personalia: Personalia,
            startDato: LocalDate,
    ) {
        aaregMap[personalia.fnr] =
                listOf(ArbeidsforholdDto.nyttArbeidsforhold(
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
        val fnr = personalia.fnr
        if (personalia.locked) {
            throw RuntimeException("Ident $fnr is locked! Cannot update!")
        }
        val arbeidsgiver: OpplysningspliktigArbeidsgiverDto
        if (arbeidsforholdType === ArbeidsgiverType.PERSON.name) {
            arbeidsgiver = PersonDto(ident, ident)
        } else {
            arbeidsgiver = OrganisasjonDto(orgnummer)
        }

        val nyttArbeidsforhold = ArbeidsforholdDto.nyttArbeidsforhold(
                fnr,
                startDato,
                sluttDato,
                stillingsprosent,
                arbeidsforholdId,
                arbeidsgiver,
        )

        val gamleArbeidsforhold = aaregMap[fnr] ?: emptyList()
        val filtrerteForhold = gamleArbeidsforhold.filter { it.arbeidsforholdId != arbeidsforholdId }
        aaregMap[fnr] = filtrerteForhold.plus(nyttArbeidsforhold)
        log.info("Legger til arbeidsforhold $fnr totalt antall: ${aaregMap[fnr]?.size ?: 0}")
    }

    fun getArbeidsforhold(fnr: String): List<ArbeidsforholdDto> {
        log.info("Henter arbeidsforhold for $fnr")
        return aaregMap[fnr] ?: emptyList()
    }

    companion object {
        private val log by logger()
    }
}
