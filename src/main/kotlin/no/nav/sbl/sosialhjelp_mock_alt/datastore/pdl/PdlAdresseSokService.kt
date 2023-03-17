package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.AdresseSokHit
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSok
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSokResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlAdresseSokResult
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Component

@Component
class PdlAdresseSokService {

  // key: "<vegadresse><husnummer><husbokstav>"
  private val nyAdresseMap: HashMap<String, AdresseSokHit> = HashMap()

  init {
    val hits = PdlAdresseSokResponse.defaultResponse().data.sokAdresse?.hits
    hits?.let { hit ->
      nyAdresseMap.putAll(
          hit.map {
            it.vegadresse.adressenavn + it.vegadresse.husnummer + it.vegadresse.husbokstav to it
          })
    }
  }

  fun getAdresse(sokestreng: String): PdlAdresseSokResponse {
    log.info("PDL adressesok, sokestreng: $sokestreng")
    val treff =
        nyAdresseMap.filter { (key, _) -> key.startsWith(sokestreng, ignoreCase = true) }.values
    return if (treff.isNotEmpty()) {
      PdlAdresseSokResponse(
          errors = null,
          data =
              PdlAdresseSok(
                  sokAdresse =
                      PdlAdresseSokResult(
                          hits = treff.toList(),
                          pageNumber = 1,
                          totalHits = treff.size,
                          totalPages = 1)))
    } else {
      PdlAdresseSokResponse.defaultResponse()
    }
  }

  companion object {
    private val log by logger()
  }
}
