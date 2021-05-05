package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.AdresseDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.AdresseSokHit
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSok
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSokResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSokResult
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Component

@Component
class PdlAdresseSokService {

    private val adresseMap: HashMap<String, PdlAdresseSokResponse> = HashMap()

    fun getAdresse(postnummer: String): PdlAdresseSokResponse? {
        log.info("Henter PDL adresse for postnummer: $postnummer")
        return adresseMap[postnummer] ?: PdlAdresseSokResponse.defaultResponse()
    }

    fun putAdresse(
        postnummer: String,
        adresse: ForenkletBostedsadresse,
        geografiskTilknytning: String? = "030102"
    ) {
        adresseMap[postnummer] = PdlAdresseSokResponse(
            errors = null,
            data = PdlAdresseSok(
                sokAdresse = PdlAdresseSokResult(
                    hits = listOf(
                        AdresseSokHit(
                            score = 0f,
                            vegadresse = AdresseDto(
                                matrikkelId = "matrikkelId",
                                husnummer = adresse.husnummer,
                                husbokstav = "",
                                adressenavn = adresse.adressenavn,
                                kommunenavn = "Test kommune",
                                kommunenummer = adresse.kommunenummer,
                                postnummer = postnummer,
                                poststed = "poststed",
                                bydelsnummer = geografiskTilknytning
                            )
                        )
                    ),
                    pageNumber = 1,
                    totalPages = 1,
                    totalHits = 1
                )
            )
        )
    }

    companion object {
        private val log by logger()
    }
}
