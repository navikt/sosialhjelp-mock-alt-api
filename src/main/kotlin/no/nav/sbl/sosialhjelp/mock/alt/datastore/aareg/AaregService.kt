package no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg

import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsforholdResponseDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsstedDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsstedType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.IdentInfoDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.IdentInfoType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AaregService {
    private val aaregMap: HashMap<String, List<ArbeidsforholdResponseDto>> = HashMap()

    fun leggTilEnkeltArbeidsforhold(
        personalia: Personalia,
        startDato: LocalDate,
        orgnummmer: String,
        stillingsprosent: Double = 100.0,
    ) {
        val arbeidssted =
            ArbeidsstedDto(
                type = ArbeidsstedType.Underenhet,
                identer =
                    listOf(
                        IdentInfoDto(
                            type = IdentInfoType.ORGANISASJONSNUMMER,
                            ident = orgnummmer,
                            gjeldende = true,
                        ),
                    ),
            )

        aaregMap[personalia.fnr] =
            listOf(
                ArbeidsforholdResponseDto.createNyttArbeidsforhold(
                    personId = personalia.fnr,
                    start = startDato,
                    arbeidssted = arbeidssted,
                    stillingsprosent = stillingsprosent,
                ),
            )
    }

    fun setArbeidsforholdForFnr(
        fnr: String,
        arbeidsforholdsliste: List<ArbeidsforholdResponseDto>,
    ) {
        aaregMap[fnr] = arbeidsforholdsliste
    }

    fun getArbeidsforhold(fnr: String): List<ArbeidsforholdResponseDto> {
        log.info("Henter arbeidsforhold for $fnr")
        return aaregMap[fnr] ?: emptyList()
    }

    companion object {
        private val log by logger()
    }
}
