package no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.AdresseDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.AdresseSokHit
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlAdresseSok
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlAdresseSokResponse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlAdresseSokResult
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlForslagAdresseAdresse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlForslagAdresseResult
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlForslagAdresseVegadresse
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Component

@Component
class PdlAdresseSokService {
    // key: "<vegadresse><husnummer><husbokstav>"
    private val nyAdresseMap: HashMap<String, AdresseSokHit> = HashMap()

    private val vegadresseListe: Map<String, AdresseDto> =
        objectMapper
            .readValue<List<AdresseDto>>(
                this::class.java.classLoader
                    .getResource("adressesok/vegadresser.json")!!
                    .readText(),
            ).map { formatVegadresse(it).uppercase() to it }
            .toMap()

    fun forslagAdresse(fritekst: String): PdlForslagAdresseResult {
        log.info("PDL forslagAdresse, sokestreng: $fritekst")

        val query = fritekst.uppercase()

        val matchingAddress = vegadresseListe.entries.firstOrNull { it.key == query }?.value

        return PdlForslagAdresseResult(
            suggestions = vegadresseListe.keys.filter { it.contains(query) },
            addressFound = matchingAddress?.toAdresseResult(),
        )
    }

    private fun AdresseDto.toAdresseResult(): PdlForslagAdresseAdresse =
        PdlForslagAdresseAdresse(
            matrikkeladresse = null,
            vegadresse =
                PdlForslagAdresseVegadresse(
                    matrikkelId = this.matrikkelId,
                    adressenavn = this.adressenavn,
                    husnummer = this.husnummer,
                    husbokstav = this.husbokstav,
                    postnummer = this.postnummer,
                    poststed = this.poststed,
                    kommunenavn = this.kommunenavn,
                    kommunenummer = this.kommunenummer,
                    bydelsnavn = "mock-bydel",
                    bydelsnummer = this.bydelsnummer,
                ),
        )

    fun formatVegadresse(vegadresse: AdresseDto): String {
        val postfiks = ", ${vegadresse.postnummer} ${vegadresse.poststed}"
        return if (!vegadresse.husbokstav.isNullOrEmpty()) {
            "${vegadresse.adressenavn} ${vegadresse.husnummer} ${vegadresse.husbokstav}$postfiks"
        } else {
            "${vegadresse.adressenavn} ${vegadresse.husnummer}$postfiks"
        }
    }

    init {

        val hits =
            PdlAdresseSokResponse
                .defaultResponse()
                .data.sokAdresse
                ?.hits
        hits?.let { hit ->
            nyAdresseMap.putAll(
                hit.map {
                    it.vegadresse.adressenavn + it.vegadresse.husnummer + it.vegadresse.husbokstav to it
                },
            )
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
                                totalPages = 1,
                            ),
                    ),
            )
        } else {
            PdlAdresseSokResponse.defaultResponse()
        }
    }

    companion object {
        private val log by logger()
    }
}
