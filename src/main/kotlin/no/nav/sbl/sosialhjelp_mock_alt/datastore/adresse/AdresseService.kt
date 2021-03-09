package no.nav.sbl.sosialhjelp_mock_alt.datastore.adresse

import no.nav.sbl.sosialhjelp_mock_alt.datastore.adresse.model.AdresseData
import no.nav.sbl.sosialhjelp_mock_alt.datastore.adresse.model.AdresseSokResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class AdresseService {

    private val adresseMap: HashMap<String, AdresseSokResponse> = HashMap()

    fun getAdresseInfo(postnummer: String): AdresseSokResponse? {
        log.info("Henter adresse for postnummer: $postnummer")
        return adresseMap[postnummer] ?: AdresseSokResponse.defaultAdressesok()
    }

    fun putAdresseInfo(postnummer: String, adresse: ForenkletBostedsadresse, geografiskTilknytning: String = "1234") {
        adresseMap[postnummer] = AdresseSokResponse(
                flereTreff = false,
                adresseDataList = listOf(AdresseData(
                        kommunenummer = adresse.kommunenummer,
                        kommunenavn = "",
                        adressenavn = adresse.adressenavn,
                        husnummerFra = "1",
                        husnummerTil = "99999",
                        postnummer = postnummer,
                        poststed = "poststed",
                        geografiskTilknytning = geografiskTilknytning,
                        gatekode = "gatekode",
                        bydel = "",
                        husnummer = adresse.husnummer.toString(),
                        husbokstav = null,
                ))
        )
    }

    companion object {
        private val log by logger()
    }
}
