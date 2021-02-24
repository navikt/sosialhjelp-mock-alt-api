package no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.OrganisasjonDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AaregService {

    private final val aaregMap: HashMap<String, List<ArbeidsforholdDto>> = HashMap()

    fun leggTilEnkeltArbeidsforhold(
            personalia: Personalia,
            startDato: LocalDate,
            orgnummmer: String,
    ) {
        val arbeidsgiver = OrganisasjonDto(
                organisasjonsnummer = orgnummmer,
        )
        aaregMap[personalia.fnr] =
                listOf(ArbeidsforholdDto.nyttArbeidsforhold(
                        fnr = personalia.fnr,
                        fom = startDato,
                        arbeidsgiver = arbeidsgiver
                ))
    }

    fun setArbeidsforholdForFnr(fnr: String, arbeidsforholdsliste: List<ArbeidsforholdDto>) {
        aaregMap[fnr] = arbeidsforholdsliste
    }

    fun getArbeidsforhold(fnr: String): List<ArbeidsforholdDto> {
        log.info("Henter arbeidsforhold for $fnr")
        return aaregMap[fnr] ?: emptyList()
    }

    companion object {
        private val log by logger()
    }
}
