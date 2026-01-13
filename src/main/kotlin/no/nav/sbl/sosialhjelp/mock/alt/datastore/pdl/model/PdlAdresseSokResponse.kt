package no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper

data class PdlAdresseSokResponse(
    val errors: List<PdlError>?,
    val data: PdlAdresseSok,
) {
    companion object {
        fun defaultResponse(): PdlAdresseSokResponse {
            val string: String =
                this::class.java.classLoader
                    .getResource("adressesok/sanner_adressesok.json")!!
                    .readText()
            return objectMapper.readValue(string)
        }
    }
}

data class PdlAdresseSok(
    val sokAdresse: PdlAdresseSokResult?,
)

data class PdlForslagAdresseVegadresse(
    val matrikkelId: String?,
    val adressenavn: String?,
    val husnummer: Int?,
    val husbokstav: String?,
    val postnummer: String?,
    val poststed: String?,
    val kommunenavn: String?,
    val kommunenummer: String?,
    val bydelsnavn: String?,
    val bydelsnummer: String?,
)

data class PdlForslagAdresseMatrikkeladresse(
    val matrikkelId: String?,
    val tilleggsnavn: String?,
    val kommunenummer: String?,
    val gaardsnummer: String?,
    val bruksnummer: String?,
    val postnummer: String?,
    val poststed: String?,
)

data class PdlForslagAdresseAdresse(
    val vegadresse: PdlForslagAdresseVegadresse?,
    val matrikkeladresse: PdlForslagAdresseMatrikkeladresse?,
)

data class PdlForslagAdresseResult(
    val suggestions: List<String>,
    val addressFound: PdlForslagAdresseAdresse?,
)

data class PdlAdresseSokResult(
    val hits: List<AdresseSokHit>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalHits: Int,
)

data class AdresseSokHit(
    val vegadresse: AdresseDto,
    val score: Float,
)

data class AdresseDto(
    val matrikkelId: String,
    val husnummer: Int,
    val husbokstav: String?,
    val adressenavn: String,
    val kommunenavn: String,
    val kommunenummer: String,
    val postnummer: String,
    val poststed: String,
    val bydelsnummer: String?,
)
