package no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsforholdDto
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
