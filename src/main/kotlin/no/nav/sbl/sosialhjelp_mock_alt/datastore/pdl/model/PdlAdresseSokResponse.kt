package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

data class PdlAdresseSokResponse(
    val errors: List<PdlError>?,
    val data: PdlAdresseSok
) {
    companion object {
        fun defaultResponse(): PdlAdresseSokResponse {
            return PdlAdresseSokResponse(
                errors = null,
                data = PdlAdresseSok(
                    sokAdresse = PdlAdresseSokResult(
                        hits = listOf(
                            AdresseSokHit(
                                score = 0f,
                                vegadresse = AdresseDto(
                                    matrikkelId = "matrikkelId123",
                                    husnummer = 2,
                                    husbokstav = null,
                                    adressenavn = "Mock veien",
                                    kommunenavn = "Mock Kommune",
                                    kommunenummer = "1000",
                                    postnummer = "0101",
                                    poststed = "Mock by",
                                    bydelsnummer = null
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
