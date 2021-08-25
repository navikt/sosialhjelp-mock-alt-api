package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper

data class PdlAdresseSokResponse(
    val errors: List<PdlError>?,
    val data: PdlAdresseSok
) {
    companion object {
        fun defaultResponse(): PdlAdresseSokResponse {
            val string: String = this::class.java.classLoader.getResource("adressesok/sanner_adressesok.json")!!.readText()
            return objectMapper.readValue(string)
        }
    }
}

data class PdlAdresseSok(
    val sokAdresse: PdlAdresseSokResult?
)

data class PdlAdresseSokResult(
    val hits: List<AdresseSokHit>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalHits: Int
)

data class AdresseSokHit(
    val vegadresse: AdresseDto,
    val score: Float
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
